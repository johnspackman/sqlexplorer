package net.sourceforge.sqlexplorer;


/*
 * Copyright (C) 2001 Colin Bell
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
 
import net.sourceforge.squirrel_sql.fw.util.log.ILogger;
import net.sourceforge.squirrel_sql.fw.util.log.Log4jLoggerFactory;

import org.apache.log4j.PropertyConfigurator;

public class SqlexplorerLoggerFactory extends Log4jLoggerFactory {
	private static ILogger s_log = null;

	public SqlexplorerLoggerFactory() throws IllegalArgumentException
	{
		super(false);
		
		/*Category.getRoot().removeAllAppenders();
		try
		{
//				final String logFileName = new ApplicationFiles().getExecutionLogFile().getPath();
//				final PatternLayout layout = new PatternLayout("%-4r [%t] %-5p %c %x - %m%n");
//				FileAppender fa = new FileAppender(layout, logFileName);
//				fa.setFile(logFileName);
				SquirrelAppender fa = new SquirrelAppender();

				BasicConfigurator.configure(fa);
				final ILogger log = createLogger(getClass());
				log.warn(
					"No logger configuration file passed on command line arguments. Using default log file: "
						+ fa.getFile() /*logFileName*//*);
			}
			catch (IOException ex)
			{
				final ILogger log = createLogger(getClass());
				log.error("Error occured configuring logging. Now logging to standard output", ex);
				BasicConfigurator.configure();
			}*/
		//BasicConfigurator.configure();
		PropertyConfigurator.configure(URLUtil.getResourceURL("log4j.properties")); //$NON-NLS-1$
		doStartupLogging();
	}

	private void doStartupLogging()
	{
		s_log = createLogger(getClass());
		s_log.info("======================================================="); //$NON-NLS-1$
		s_log.info("======================================================="); //$NON-NLS-1$
		s_log.info("======================================================="); //$NON-NLS-1$
		s_log.info("java.vendor: " + System.getProperty("java.vendor")); //$NON-NLS-1$ //$NON-NLS-2$
		s_log.info("java.version: " + System.getProperty("java.version")); //$NON-NLS-1$ //$NON-NLS-2$
		s_log.info("java.runtime.name: " + System.getProperty("java.runtime.name")); //$NON-NLS-1$ //$NON-NLS-2$
		s_log.info("os.name: " + System.getProperty("os.name")); //$NON-NLS-1$ //$NON-NLS-2$
		s_log.info("os.version: " + System.getProperty("os.version")); //$NON-NLS-1$ //$NON-NLS-2$
		s_log.info("os.arch: " + System.getProperty("os.arch")); //$NON-NLS-1$ //$NON-NLS-2$
		s_log.info("user.dir: " + System.getProperty("user.dir")); //$NON-NLS-1$ //$NON-NLS-2$
		s_log.info("user.home: " + System.getProperty("user.home")); //$NON-NLS-1$ //$NON-NLS-2$
		s_log.info("java.home: " + System.getProperty("java.home")); //$NON-NLS-1$ //$NON-NLS-2$
		s_log.info("java.class.path: " + System.getProperty("java.class.path")); //$NON-NLS-1$ //$NON-NLS-2$
	}

}
