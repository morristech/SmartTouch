package com.pocket.network;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Locale;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.conn.ConnectionReleaseTrigger;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRoute;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.auth.DigestScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import com.zzy.smarttouch.Common;


public class CostomHttpClient
{
	private static final int maxConnectionToRoute = 5;
	public DefaultHttpClient client;
	private HttpPool threadpool;
	public HttpInterface netInterface;

	public CostomHttpClient()
			throws ClientProtocolException,IOException
	{
	    threadpool = new HttpPool();
	}
	
	public void Initialize(HttpInterface netInterface)
	{
		this.netInterface = netInterface;
		initializeShared(SSLSocketFactory.getSocketFactory());
	}
	
	public void SetNetCallback(HttpInterface netInterface)
	{
		this.netInterface = netInterface;
	}

	private void initializeShared(SSLSocketFactory ssl)
	{
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		schemeRegistry.register(new Scheme("https", ssl, 443));
		HttpParams params = new BasicHttpParams();
		ConnManagerParams.setMaxConnectionsPerRoute(params, new ConnPerRoute()
		{
			public int getMaxForRoute(HttpRoute route)
			{
				return maxConnectionToRoute;
			}
		});
		ConnManagerParams.setTimeout(params, 100L);
		ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);
		this.client = new DefaultHttpClient(cm, params);
		this.client.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(0, false));
	}

	public void InitializeAcceptAll(HttpInterface netInterface)
			throws KeyManagementException, NoSuchAlgorithmException,
			SecurityException, NoSuchMethodException, IllegalArgumentException,
			InstantiationException, IllegalAccessException,
			InvocationTargetException
	{
		this.netInterface = netInterface;
		TrustManager[] tm = { new NaiveTrustManager() };
		SSLContext context = SSLContext.getInstance("TLS");
		context.init(new KeyManager[0], tm, new SecureRandom());
		Constructor<SSLSocketFactory> c = SSLSocketFactory.class.getConstructor(new Class[] { javax.net.ssl.SSLSocketFactory.class });
		SSLSocketFactory ssl = (SSLSocketFactory) c.newInstance(new Object[] { context.getSocketFactory() });
		ssl.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		initializeShared(ssl);
	}

	public boolean IsInitialized()
	{
		return this.client != null;
	}

	public void SetHttpParameter(String Name, Object Value)
	{
		this.client.getParams().setParameter(Name, Value);
	}

	public void SetProxy(String Host, int Port, String Scheme)
	{
		HttpHost hh = new HttpHost(Host, Port, Scheme);
		this.client.getParams().setParameter("http.route.default-proxy", hh);
	}

	public void SetProxy2(String Host, int Port, String Scheme,String Username, String Password)
	{
		HttpHost hh = new HttpHost(Host, Port, Scheme);
		this.client.getCredentialsProvider().setCredentials(
				new AuthScope(Host, Port),
				new UsernamePasswordCredentials(Username, Password));
		this.client.getParams().setParameter("http.route.default-proxy", hh);
	}

	public boolean Execute(HttpUriRequestWrapper HttpRequest, int TaskId)
			throws ClientProtocolException, IOException
	{
		return ExecuteCredentials(HttpRequest, TaskId, null, null);
	}

	public boolean ExecuteCredentials(HttpUriRequestWrapper HttpRequest,int TaskId, String UserName, String Password)
			throws ClientProtocolException, IOException
	{
		if(threadpool==null)
		{
			return false;
		}
		if (threadpool.isRunning(this, TaskId))
		{
			return false;
		}
		Runnable runnable = new ExecuteHelper(HttpRequest, TaskId,UserName, Password);
		threadpool.submit(runnable, this, TaskId);
		return true;
	}

	private HttpResponse executeWithTimeout(Runnable handler,HttpUriRequest req,int TaskId)
			throws ClientProtocolException, IOException
	{
		try
		{
			HttpResponse response = this.client.execute(req);
			return response;
		}
		catch (ConnectionPoolTimeoutException cpte)
		{
			cpte.printStackTrace();
			Common.LogEx("executeWithTimeout");
			HttpPool.handler.postDelayed(new ConnectionPoolRunnable(handler, TaskId), 2000L);
		}
		return null;
	}
	
	class ConnectionPoolRunnable implements Runnable
	{
		private Runnable handler;
		private int TaskId;

		public ConnectionPoolRunnable(Runnable handler, int TaskId)
		{
			this.handler = handler;
			this.TaskId = TaskId;
		}

		public void run()
		{
			threadpool.submit(this.handler, CostomHttpClient.this, this.TaskId);
		}
	}

	class ExecuteHelper implements Runnable
	{
		private HttpUriRequestWrapper HttpRequest;
		private int TaskId;
		private String UserName;
		private String Password;

		public ExecuteHelper(HttpUriRequestWrapper HttpRequest,int TaskId, String UserName, String Password)
		{
			this.HttpRequest = HttpRequest;
			this.TaskId = TaskId;
			this.UserName = UserName;
			this.Password = Password;
		}

		public void run()
		{
			Common.LogEx("current thread"+Thread.currentThread().getName());
			HttpResponse response = null;
			try
			{
				if (((this.HttpRequest.req instanceof HttpEntityEnclosingRequestBase))
						&& (this.UserName != null)
						&& (this.UserName.length() > 0))
				{
					HttpEntityEnclosingRequestBase base = (HttpEntityEnclosingRequestBase) this.HttpRequest.req;
					if ((base.getEntity() != null)&& (!base.getEntity().isRepeatable()))
					{
						UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(this.UserName, this.Password);
						BasicScheme scheme = new BasicScheme();
						Header authorizationHeader = scheme.authenticate(credentials, this.HttpRequest.req);
						this.HttpRequest.req.addHeader(authorizationHeader);
					}
				}
				response = CostomHttpClient.this.executeWithTimeout(this,this.HttpRequest.req, this.TaskId);
				if (response == null)
				{
					return;
				}
				
				if ((response.getStatusLine().getStatusCode() == 401)
						&& (this.UserName != null)
						&& (this.UserName.length() > 0))
				{
					boolean basic = false;
					boolean digest = false;
					Header challenge = null;
					for (Header h : response.getHeaders("WWW-Authenticate"))
					{
						String v = h.getValue().toLowerCase(Locale.US);
						if (v.contains("basic"))
						{
							basic = true;
						} else if (v.contains("digest"))
						{
							digest = true;
							challenge = h;
						}
					}

					UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(this.UserName, this.Password);
					if (response.getEntity() != null)
					{
						response.getEntity().consumeContent();
					}
					if (digest)
					{
						DigestScheme ds = new DigestScheme();
						ds.processChallenge(challenge);
						this.HttpRequest.req.addHeader(ds.authenticate(credentials, this.HttpRequest.req));
						response = CostomHttpClient.this.executeWithTimeout(this, this.HttpRequest.req,this.TaskId);
						if (response == null)
						{
							return;
						}
					} 
					else if (basic)
					{
						BasicScheme scheme = new BasicScheme();
						Header authorizationHeader = scheme.authenticate(credentials, this.HttpRequest.req);
						this.HttpRequest.req.addHeader(authorizationHeader);
						response = CostomHttpClient.this.executeWithTimeout(this, this.HttpRequest.req,this.TaskId);
						if (response == null)
						{
							return;
						}
					}
				}
				if (response.getStatusLine().getStatusCode() / 100 != 2)
				{
					throw new Exception();
				}
				HttpResponeWrapper res = new HttpResponeWrapper();
				res.innerInitialize(CostomHttpClient.this,response);
				CostomHttpClient.this.netInterface.net_responsesuccess(res,this.TaskId);
			} 
			catch (Exception e)
			{
				int statusCode;
				String reason;
				if (response != null)
				{
					reason = response.getStatusLine().getReasonPhrase();
					statusCode = response.getStatusLine().getStatusCode();
				} 
				else
				{
					e.printStackTrace();
					reason = e.toString();
					statusCode = -1;
				}

				HttpResponeWrapper res = null;
				if (response != null)
				{
					res = new HttpResponeWrapper();
					res.innerInitialize(CostomHttpClient.this,response);
					try
					{
						response.setEntity(new ByteArrayEntity(EntityUtils.toByteArray(response.getEntity())));
					}
					catch (Exception ee)
					{
						ee.printStackTrace();
					}
				}
				CostomHttpClient.this.netInterface.net_responseerror(res,reason ,statusCode,this.TaskId);
				if ((response != null)&& (response.getEntity() != null))
				try
				{
					response.getEntity().consumeContent();
				} 
				catch (IOException e1)
				{
					Log.w("B4A", e1);
				}
			}
		}
	}

	public static class HttpResponeWrapper
	{
		private CostomHttpClient parent;
		private HttpResponse response;
		private OutputStream Output;
		private boolean CloseOutput; 
		private int TaskId;

		public void innerInitialize(CostomHttpClient parent,HttpResponse response)
		{
			this.parent = parent;
			this.response = response;
		}
		
		
		public InputStream GetInputStream()
				throws IllegalStateException, IOException
		{
			return this.response.getEntity().getContent();
		}

		public String GetString(String DefaultCharset) 
				throws ParseException,IOException
		{
			if (this.response.getEntity() == null)
			{
				return "";
			}
			return EntityUtils.toString(this.response.getEntity(),DefaultCharset);
		}

		public Header[] GetHeaders()
		{
			return this.response.getAllHeaders();
		}

		public String getContentType()
		{
			return this.response.getEntity().getContentType().getValue();
		}

		public void Release() throws IOException
		{
			if ((this.response != null) && (this.response.getEntity() != null))
			{
				this.response.getEntity().consumeContent();
			}
		}

		public String getContentEncoding()
		{
			return this.response.getEntity().getContentEncoding().getValue();
		}

		public long getContentLength()
		{
			return this.response.getEntity().getContentLength();
		}

		public int getStatusCode()
		{
			return this.response.getStatusLine().getStatusCode();
		}

		public boolean GetAsynchronously(OutputStream Output, boolean CloseOutput, int TaskId)
				throws IOException
		{
			this.Output = Output;
			this.TaskId = TaskId;
			this.CloseOutput = CloseOutput;
//			if (parent.threadpool.isRunning(this.parent, TaskId))
//			{
//				Release();
//				return false;
//			}

			Runnable runnable = new Runnable()
			{
				public void run()
				{
					boolean abortConnection = false;
					try
					{
						HttpResponeWrapper.this.response.getEntity().writeTo(HttpResponeWrapper.this.Output);
						if (HttpResponeWrapper.this.CloseOutput)
						{
							HttpResponeWrapper.this.Output.close();
						}
						
						parent.netInterface.net_streamfinish(true,HttpResponeWrapper.this.TaskId);
					} 
					catch (IOException e)
					{
						abortConnection = true;
						if (HttpResponeWrapper.this.CloseOutput)
						{
							try
							{
								HttpResponeWrapper.this.Output.close();
							} 
							catch (IOException e1)
							{
								Log.w("B4A", e1.getMessage());
							}
						}
						parent.netInterface.net_streamfinish(false,HttpResponeWrapper.this.TaskId);
					}
					
					try
					{
						if ((abortConnection)
						    && (HttpResponeWrapper.this.response.getEntity() instanceof ConnectionReleaseTrigger))
						{
							((ConnectionReleaseTrigger)HttpResponeWrapper.this.response.getEntity()).abortConnection();
						}
						else
						{
							HttpResponeWrapper.this.response.getEntity().consumeContent();
						}
					} 
					catch (IOException e)
					{
						Log.w("B4A", e.getMessage());
					}
				}
			};
			parent.threadpool.submit(runnable, this.parent, TaskId);
			return true;
		}
	}

	public static class HttpUriRequestWrapper
	{
		private boolean POST;
		private AbstractHttpEntity entity;

		public HttpRequestBase req;

		public void InitializeGet(String URL)
		{
			this.req = new HttpGet(URL);
			this.POST = false;
			sharedInit();
		}

		public void InitializeHead(String URL)
		{
			this.req = new HttpHead(URL);
			this.POST = false;
			sharedInit();
		}

		public void InitializeDelete(String URL)
		{
			this.req = new HttpDelete(URL);
			this.POST = false;
			sharedInit();
		}

		public void InitializePost(String URL, InputStream InputStream,
				int Length)
		{
			HttpPost post = new HttpPost(URL);
			this.req = post;
			this.entity = new InputStreamEntity(InputStream, Length);
			post.setEntity(this.entity);
			this.entity.setContentType("application/x-www-form-urlencoded");
			this.POST = true;
			sharedInit();
		}

		public void InitializePut(String URL, InputStream InputStream,
				int Length)
		{
			HttpPut post = new HttpPut(URL);
			this.req = post;
			this.entity = new InputStreamEntity(InputStream, Length);
			post.setEntity(this.entity);
			this.entity.setContentType("application/x-www-form-urlencoded");
			this.POST = true;
			sharedInit();
		}

		public void InitializePost2(String URL, byte[] Data)
		{
			HttpPost post = new HttpPost(URL);
			this.req = post;
			this.entity = new ByteArrayEntity(Data);
			post.setEntity(this.entity);
			this.entity.setContentType("application/x-www-form-urlencoded");
			this.POST = true;
			sharedInit();
		}

		public void InitializePut2(String URL, byte[] Data)
		{
			HttpPut post = new HttpPut(URL);
			this.req = post;
			this.entity = new ByteArrayEntity(Data);
			post.setEntity(this.entity);
			this.entity.setContentType("application/x-www-form-urlencoded");
			this.POST = true;
			sharedInit();
		}

		private void sharedInit()
		{
			setTimeout(30000);
		}

		public void SetContentType(String ContentType)
		{
			if (!this.POST)
			{
				throw new RuntimeException("Only Post / Put requests support this method.");
			}
			this.entity.setContentType(ContentType);
		}

		public void SetContentEncoding(String Encoding)
		{
			if (!this.POST)
				throw new RuntimeException(
						"Only Post / Put requests support this method.");
			this.entity.setContentEncoding(Encoding);
		}

		public void setTimeout(int Timeout)
		{
			HttpConnectionParams.setConnectionTimeout(this.req.getParams(),
					Timeout);
			HttpConnectionParams.setSoTimeout(this.req.getParams(), Timeout);
		}

		public void SetHeader(String Name, String Value)
		{
			this.req.setHeader(Name, Value);
		}

		public void RemoveHeaders(String Name)
		{
			this.req.removeHeaders(Name);
		}
	}

	private static class NaiveTrustManager implements X509TrustManager
	{
		public void checkClientTrusted(X509Certificate[] cert, String authType)
				throws CertificateException
		{
		}

		public void checkServerTrusted(X509Certificate[] cert, String authType)
				throws CertificateException
		{
		}

		public X509Certificate[] getAcceptedIssuers()
		{
			return null;
		}
	}
}