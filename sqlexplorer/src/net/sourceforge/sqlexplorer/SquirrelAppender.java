package net.sourceforge.sqlexplorer;

/*
 * Copyright (C) 2001-2002-2004 Colin Bell
 * colbell@users.sourceforge.net
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;

/**
 * This log4j appender writes to the Eclipse error logging facility.
 */
public class SquirrelAppender extends AppenderSkeleton
{
	public SquirrelAppender()
	{
		super();
	}

	protected void append(LoggingEvent event)
	{
		ThrowableInformation throwInfo = event.getThrowableInformation();
		Throwable throwable = null;
		
		if (throwInfo != null)
		{
			throwable = throwInfo.getThrowable();
		}
		
		SQLExplorerPlugin.error(event.getRenderedMessage(), throwable);
	}
	
	public void close() {
	}
	
	public boolean requiresLayout() {
		return false;
	}
}
