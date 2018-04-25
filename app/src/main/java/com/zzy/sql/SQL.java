package com.zzy.sql;

import java.io.File;
import java.util.List;

import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

public class SQL
{
	private SQLiteDatabase db;

	public SQL(String Dir, String FileName, boolean CreateIfNecessary)
	{
		this.db = SQLiteDatabase.openDatabase(new File(Dir, FileName).toString(), null, (CreateIfNecessary ? 268435456
		        : 0) | 0x10);
	}

	private boolean checkNull()
	{
		if (this.db == null || !this.db.isOpen())
		{
			return false;
		}
		return true;
	}

	public boolean IsInitialized()
	{
		if (this.db == null)
		{
			return false;
		}
		return this.db.isOpen();
	}

	public void ExecNonQuery(String Statement)
	{
		if (checkNull())
		{
			this.db.execSQL(Statement);
		}
	}

	public void ExecNonQuery2(String Statement, List<Object> Args)
	{
		SQLiteStatement s = this.db.compileStatement(Statement);
		try
		{
			int numArgs = 0;
			if (Args != null)
			{
				numArgs = Args.size();
			}
			for (int i = 0; i < numArgs; i++)
			{
				DatabaseUtils.bindObjectToProgram(s, i + 1, Args.get(i));
			}
			s.execute();
		}
		finally
		{
			s.close();
		}
	}

	public Cursor ExecQuery(String Query)
	{
		return ExecQuery2(Query, null);
	}

	public Cursor ExecQuery2(String Query, String[] StringArgs)
	{
		if (checkNull())
		{
			return this.db.rawQuery(Query, StringArgs);
		}
		return null;
	}

	public String ExecQuerySingleResult(String Query)
	{
		return ExecQuerySingleResult2(Query, null);
	}

	public String ExecQuerySingleResult2(String Query, String[] StringArgs)
	{
		if (!checkNull())
		{
			return null;
		}
		Cursor cursor = this.db.rawQuery(Query, StringArgs);
		try
		{
			if (cursor == null)
			{
				return null;
			}

			if (cursor.getColumnCount() == 0 || cursor.getCount() == 0)
			{
				return null;
			}

			cursor.moveToFirst();
			String str = cursor.getString(0);
			return str;
		}
		finally
		{
			cursor.close();
		}
	}

	public void BeginTransaction()
	{
		if (!checkNull())
		{
			return;
		}
		this.db.beginTransaction();
	}

	public void TransactionSuccessful()
	{
		if (!checkNull())
		{
			return;
		}
		this.db.setTransactionSuccessful();
	}

	public void EndTransaction()
	{
		if (!checkNull())
		{
			return;
		}
		this.db.endTransaction();
	}

	public void Close()
	{
		if ((this.db != null) && (this.db.isOpen()))
		{
			this.db.close();
		}
	}
}
