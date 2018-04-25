package com.pocket.network;

import com.pocket.network.CostomHttpClient.HttpResponeWrapper;

public interface HttpInterface
{
	public void net_responsesuccess(HttpResponeWrapper res, int iTaskId);
	public void net_responseerror(HttpResponeWrapper res, String sReason, int iStatusCode, int iTaskId);
	public void net_streamfinish(boolean bRet, int iTask);
}
