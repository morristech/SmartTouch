package com.zzy.sql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import com.zzy.privacy.PrivacyAdpter.LockAppSt;
import com.zzy.smarttouch.MainClickAdpter;
import com.zzy.smarttouch.smartKeyApp;
import com.zzy.smarttouch.MainClickAdpter.EventSt;
import com.zzy.smarttouch.MainClickAdpter.ProgrameSt;

import android.R.integer;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;

public class Config
{

	public final static String DBNAME = "smartKey.db";
	public final static String EVENT_TABLE = "event";
	public final static String CONFIG_TABLE = "config";
	public final static String SWITCH_TABLE = "switch";
	public final static String LOCK_TABLE = "lock";

	public final static String UPDATE_APK_TIME = "update_apk_time";
	public final static String REGEDIT_CODE = "regedit_code";
	public final static String REGEDIT_DATA = "regedit_data";
	public final static String UPDATE_GAME_TIME = "update_game_time";
	public final static String DOUBLE_CLICK_SOUND = "DOUBLE_CLICK_SOUND";
	public final static String DOUBLE_CLICK_VIBRATE = "DOUBLE_CLICK_VIBRATE";
	public final static String SERVICE_ENABLE = "SERVICE_ENABLE";
	public final static String FIRST_USE ="first_use";
	private SQL sql;

	public Config(String Dir, String FileName, boolean CreateIfNecessary)
	{
		sql = new SQL(Dir, FileName, CreateIfNecessary);
	}

	public boolean isOpen()
	{
		if (sql == null)
		{
			return false;
		}
		return sql.IsInitialized();
	}

	public void CreateTable(String sTableName, HashMap<Object, Object> FieldsAndTypes, String PrimaryKey,
	        boolean bAUTOINCREMENT)
	{
		if (sql == null)
		{
			return;
		}
		StringBuilder sb = new StringBuilder();
		sb = sb.append("(");

		Iterator<Entry<Object, Object>> iterator = FieldsAndTypes.entrySet().iterator();
		boolean bFirst = true;
		while (iterator.hasNext())
		{
			Entry<Object, Object> next = iterator.next();
			if (!bFirst)
			{
				sb = sb.append(", ");
			}
			sb = sb.append("[").append(next.getKey()).append("] ").append(next.getValue());

			if (PrimaryKey != null && next.getKey().toString().equalsIgnoreCase(PrimaryKey))
			{
				sb.append(" PRIMARY KEY");
				if (bAUTOINCREMENT)
				{
					sb = sb.append(" AUTOINCREMENT");
				}
			}
			bFirst = false;
		}
		sb = sb.append(")");
		// String sQuery =
		// "create table if not exists config(name char(256),value char(256))";
		String sQuery = "CREATE TABLE IF NOT EXISTS [" + sTableName + "] " + sb.toString();
		sql.ExecNonQuery(sQuery);
	}

	public void DropTable(String sTableName)
	{
		if (sql == null)
		{
			return;
		}
		String sQuery = "DROP TABLE IF EXISTS [" + sTableName + "]";
		if (sql != null)
		{
			sql.ExecNonQuery(sQuery);
		}
	}

	public void InsertMaps(String sTableName, ArrayList<HashMap<String, Object>> ListOfMaps)
	{
		if (sql == null)
		{
			return;
		}
		boolean bFirst = true;
		if (ListOfMaps == null || ListOfMaps.size() < 1)
		{
			return;
		}

		sql.BeginTransaction();
		for (int i = 0; i < ListOfMaps.size(); i++)
		{
			bFirst = true;
			StringBuilder sb = new StringBuilder();
			StringBuilder columns = new StringBuilder();
			StringBuilder values = new StringBuilder();
			sb = sb.append("INSERT INTO [" + sTableName + "] (");

			ArrayList<Object> listOfValues = new ArrayList<Object>();
			HashMap<String, Object> map = ListOfMaps.get(i);

			Iterator<Entry<String, Object>> iterator = map.entrySet().iterator();
			while (iterator.hasNext())
			{
				Entry<String, Object> next = iterator.next();
				if (!bFirst)
				{
					columns = columns.append(", ");
					values = values.append(", ");
				}
				columns.append("[").append(next.getKey()).append("]");
				values.append("?");
				listOfValues.add(next.getValue());
				bFirst = false;
			}
			sb = sb.append(columns.toString()).append(") VALUES (").append(values.toString()).append(")");
			sql.ExecNonQuery2(sb.toString(), listOfValues);
		}
		sql.TransactionSuccessful();
		sql.EndTransaction();
	}

	public void UpdateRecord(String sTableName, String sField, Object sNewValue,
	        HashMap<Object, Object> WhereFieldEquals)
	{
		if (sql == null)
		{
			return;
		}
		StringBuilder sb = new StringBuilder();
		sb = sb.append("UPDATE [").append(sTableName).append("] SET [").append(sField).append("] = ? WHERE ");
		if (WhereFieldEquals.size() == 0)
		{
			return;
		}
		ArrayList<Object> listArgs = new ArrayList<Object>();
		listArgs.add(sNewValue);

		boolean bFirst = true;
		Iterator<Entry<Object, Object>> iterator = WhereFieldEquals.entrySet().iterator();
		while (iterator.hasNext())
		{
			Entry<Object, Object> next = iterator.next();
			if (!bFirst)
			{
				sb = sb.append(" AND ");
			}
			sb = sb.append("[").append(next.getKey()).append("] = ?");
			listArgs.add(next.getValue());
		}
		sql.ExecNonQuery2(sb.toString(), listArgs);
	}

	public void UpdateRecord2(String sTableName, HashMap<Object, Object> sFields,
	        HashMap<Object, Object> WhereFieldEquals)
	{
		if (sql == null)
		{
			return;
		}
		if (WhereFieldEquals == null || WhereFieldEquals.size() < 1 || sFields == null || sFields.size() < 1)
		{
			return;
		}

		StringBuilder sb = new StringBuilder();
		sb = sb.append("UPDATE [").append(sTableName).append("] SET ");
		ArrayList<Object> ListArgs = new ArrayList<Object>();
		boolean bFirst = true;

		Iterator<Entry<Object, Object>> iterator = sFields.entrySet().iterator();
		while (iterator.hasNext())
		{
			Entry<Object, Object> next = iterator.next();
			if (!bFirst)
			{
				sb = sb.append(",");
			}
			sb = sb.append("[").append(next.getKey()).append("]=?");
			ListArgs.add(next.getValue());
		}

		sb = sb.append(" WHERE ");
		iterator = WhereFieldEquals.entrySet().iterator();
		while (iterator.hasNext())
		{
			Entry<Object, Object> next = iterator.next();
			if (!bFirst)
			{
				sb = sb.append(" AND ");
			}
			sb = sb.append("[").append(next.getKey()).append("] = ?");
		}
		sql.ExecNonQuery2(sb.toString(), ListArgs);
	}

	public void DeleteRecord(String sTableName)
	{
		if (sql == null)
		{
			return;
		}
		String sQuery = "DELETE  FROM [" + sTableName + "]";
		sql.ExecNonQuery(sQuery);
	}

	public void DeleteRecord(String sTableName, String sRecord)
	{
		if (sql == null)
		{
			return;
		}
		String sQuery = "DELETE  FROM [" + sTableName + "] where " + sRecord;
		sql.ExecNonQuery(sQuery);
	}

	public boolean IsExistTable(String sTable)
	{
		long count;
		if (sql == null)
		{
			return false;
		}
		count = Integer.parseInt(sql
		        .ExecQuerySingleResult("SELECT count(*) FROM sqlite_master WHERE Type='table' AND name='" + sTable
		                + "'"));
		if (count > 0)
		{
			return true;
		}
		return false;
	}

	public Cursor ExecuteMemoryTable(String sQuery, String StringArgs[])
	{
		Cursor cur;
		if (sql == null)
		{
			return null;
		}
		if (StringArgs == null || StringArgs.length < 1)
		{
			cur = sql.ExecQuery(sQuery);
		}
		else
		{
			cur = sql.ExecQuery2(sQuery, StringArgs);
		}
		return cur;
	}

	public void close()
	{
		if (sql == null)
		{
			return;
		}
		sql.Close();
	}

	public void SetConfig(String sName, String sValue)
	{
		try
		{
			DeleteRecord(CONFIG_TABLE, "name='" + sName + "'");
			ArrayList<HashMap<String, Object>> listConfig = new ArrayList<HashMap<String, Object>>();
			HashMap<String, Object> KeyMap = new HashMap<String, Object>();
			KeyMap.put("name", sName);
			KeyMap.put("value", sValue);
			listConfig.add(KeyMap);
			InsertMaps(CONFIG_TABLE, listConfig);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public String GetConfig(String sName)
	{
		String sConfigName = null;
		Cursor cur = null;
		try
		{
			String sQuery = "Select value From " + CONFIG_TABLE + " Where name=?";
			String StringArgs[] = { sName };
			cur = ExecuteMemoryTable(sQuery, StringArgs);
			if (cur == null)
			{
				return sConfigName;
			}

			if (cur.getCount() > 0)
			{
				cur.moveToFirst();
				sConfigName = cur.getString(cur.getColumnIndex("value"));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (cur != null)
			{
				cur.close();
			}
		}
		return sConfigName;
	}

	public void saveEventList(ArrayList<EventSt> lsEvent)
	{
		try
		{
			DeleteRecord(EVENT_TABLE);
			HashMap<String, Object> hashMap;
			ArrayList<HashMap<String, Object>> listConfig = new ArrayList<HashMap<String, Object>>();

			for (int i = 0; i < lsEvent.size(); i++)
			{
				EventSt stEvent = lsEvent.get(i);
				if (stEvent.stPrograme == null)
				{
					continue;
				}

				hashMap = new HashMap<String, Object>();
				hashMap.put("id", stEvent.stPrograme.iId);
				hashMap.put("state", stEvent.iClickState);
				hashMap.put("packName", stEvent.stPrograme.sPackName);
				hashMap.put("apkname",stEvent.stPrograme.sAppName);
				if(stEvent.stPrograme.sApkUrl==null)
				{
					stEvent.stPrograme.sApkUrl ="";
				}
				hashMap.put("apkurl",stEvent.stPrograme.sApkUrl);
				
				if(stEvent.stPrograme.sApkPicName==null)
				{
					stEvent.stPrograme.sApkPicName ="";
				}
				hashMap.put("apkpicName",stEvent.stPrograme.sApkPicName);
				
				if(stEvent.stPrograme.sApkPicUrl==null)
				{
					stEvent.stPrograme.sApkPicUrl ="";
				}
				hashMap.put("apkpicUrl",stEvent.stPrograme.sApkPicUrl);
				
				listConfig.add(hashMap);
			}
			InsertMaps(EVENT_TABLE, listConfig);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void FillEventList(ArrayList<EventSt> lsEvent)
	{
		Cursor cur = null;
		try
		{
			String sQuery = "Select id,state,packName,apkname,apkurl,apkpicName,apkpicUrl From " + EVENT_TABLE;
			cur = ExecuteMemoryTable(sQuery, null);
			if (cur == null)
			{
				return;
			}

			int iCount = cur.getCount();
			if (iCount < 1)
			{
				cur.close();
				return;
			}
			cur.moveToFirst();

			do
			{
				for (int j = 0; j < smartKeyApp.CLICK_STATE_MAX; j++)
				{
					EventSt stEvent = lsEvent.get(j);

					if (cur.getInt(1) == stEvent.iClickState)
					{
						stEvent.stPrograme = new ProgrameSt();
						stEvent.stPrograme.sPackName = cur.getString(2);
						stEvent.stPrograme.sAppName = cur.getString(3);
						stEvent.stPrograme.sApkUrl = cur.getString(4);
						stEvent.stPrograme.iId = cur.getInt(0);
						stEvent.stPrograme.sApkPicName = cur.getString(5);
						stEvent.stPrograme.sApkPicUrl = cur.getString(6);
						if (stEvent.stPrograme.sPackName != null && stEvent.stPrograme.sPackName.length() > 0)
						{
							PackageManager pm = smartKeyApp.mInstance.getApplicationContext().getPackageManager();
							try
                            {
								Drawable drawable = pm.getApplicationIcon(stEvent.stPrograme.sPackName);
								if (drawable != null)
								{
									stEvent.stPrograme.drIcon =drawable; 
								}
								else
								{
									stEvent.stPrograme.drIcon = smartKeyApp.mInstance.drApkDefault;
								}
                            }
                            catch (Exception e)
                            {
                            	stEvent.stPrograme.drIcon = smartKeyApp.mInstance.drApkDefault;
                            }
						}
						break;
					}
				}
			} while (cur.moveToNext());

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (cur != null)
			{
				cur.close();
			}
		}
	}

	public void saveSwitchList(ArrayList<ProgrameSt> lsPrograme)
	{
		try
		{
			DeleteRecord(SWITCH_TABLE);
			HashMap<String, Object> hashMap;
			ArrayList<HashMap<String, Object>> listConfig = new ArrayList<HashMap<String, Object>>();

			for (int i = 0; i < lsPrograme.size(); i++)
			{
				ProgrameSt stPrograme = lsPrograme.get(i);
				if (stPrograme == null)
				{
					continue;
				}

				hashMap = new HashMap<String, Object>();
				hashMap.put("id", stPrograme.iId);
				hashMap.put("packName", stPrograme.sPackName);
				listConfig.add(hashMap);
			}
			InsertMaps(SWITCH_TABLE, listConfig);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void GetSwitchList(ArrayList<ProgrameSt> lsPrograme)
	{
		Cursor cur = null;
		try
		{
			String sQuery = "Select id,packName From " + SWITCH_TABLE;
			cur = ExecuteMemoryTable(sQuery, null);
			if (cur == null)
			{
				return;
			}

			int iCount = cur.getCount();
			if (iCount > 0)
			{
				cur.moveToFirst();

				do
				{

					ProgrameSt stPrograme = new ProgrameSt();
					stPrograme.iId = cur.getInt(0);
					stPrograme.sPackName = cur.getString(1);
					if (stPrograme.sPackName == null || stPrograme.sPackName.length() < 1)
					{
						stPrograme.drIcon = null;
						stPrograme.sPackName = "";
					}
					else
					{
						PackageManager pm = smartKeyApp.mInstance.getApplicationContext().getPackageManager();
						CharSequence cs = pm.getApplicationLabel(pm.getApplicationInfo(stPrograme.sPackName, 0));
						if (cs != null)
						{
							stPrograme.sAppName = cs.toString();
						}
						else
						{
							stPrograme.sAppName = "Unknown";
						}
						stPrograme.drIcon = pm.getApplicationIcon(stPrograme.sPackName);
					}

					lsPrograme.add(stPrograme);

				} while (cur.moveToNext());
			}

			if (lsPrograme.size() < 6)
			{
				for (int i = lsPrograme.size(); i < 6; i++)
				{
					ProgrameSt stPrograme = new ProgrameSt();
					stPrograme.iId = i;
					stPrograme.drIcon = null;
					stPrograme.sPackName = "";
					lsPrograme.add(stPrograme);
				}
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (cur != null)
			{
				cur.close();
			}
		}
	}

	public void saveLockList(LockAppSt lockAppSt)
	{
		try
		{
			DeleteRecord(LOCK_TABLE, "name='" + lockAppSt.sPackName + "'");
			String sSql = "insert into " + LOCK_TABLE + " VALUES ('" + lockAppSt.sPackName + "')";
			if (sql == null)
			{
				return;
			}
			sql.ExecNonQuery(sSql);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void DelectLockByPackName(String sName)
	{
		DeleteRecord(LOCK_TABLE, "name='" + sName + "'");
	}

	public void GetLockList(ArrayList<String> lsLockApp)
	{
		Cursor cur = null;
		try
		{
			String sQuery = "Select * From " + LOCK_TABLE;
			cur = ExecuteMemoryTable(sQuery, null);
			if (cur == null)
			{
				return;
			}

			int iCount = cur.getCount();
			if (iCount < 1)
			{
				return;
			}
			cur.moveToFirst();

			do
			{
				String sName =cur.getString(0);
				lsLockApp.add(sName);
			} while (cur.moveToNext());

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (cur != null)
			{
				cur.close();
			}
		}
	}

}
