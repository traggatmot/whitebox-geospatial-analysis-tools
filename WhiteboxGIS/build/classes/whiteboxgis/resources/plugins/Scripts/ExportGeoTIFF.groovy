/*
 * Copyright (C) 2014 Dr. John Lindsay <jlindsay@uoguelph.ca>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 
import java.awt.event.ActionListener
import java.awt.event.ActionEvent
import java.io.File
import java.util.Date
import java.util.ArrayList
import java.util.Arrays
import whitebox.interfaces.WhiteboxPluginHost
import whitebox.geospatialfiles.WhiteboxRaster
import whitebox.geospatialfiles.WhiteboxRasterBase
import whitebox.ui.plugin_dialog.ScriptDialog
import whitebox.utilities.FileUtilities;
import whitebox.utilities.Topology;
import whitebox.interfaces.InteropPlugin.InteropPluginType
import groovy.transform.CompileStatic

// The following four variables are required for this 
// script to be integrated into the tool tree panel. 
// Comment them out if you want to remove the script.
def name = "ExportGeoTIFF"
def descriptiveName = "Export GeoTIFF"
def description = "Exports a Whitebox GAT Raster to GeoTIFF format (.tif)"
def toolboxes = ["IOTools"]

public class ExportGeoTIFF implements ActionListener {
    private WhiteboxPluginHost pluginHost
    private ScriptDialog sd;
    private String descriptiveName
	private String progressLabel = "Progress:"
            	
    public ExportGeoTIFF(WhiteboxPluginHost pluginHost, 
        String[] args, def name, def descriptiveName) {
        this.pluginHost = pluginHost
        this.descriptiveName = descriptiveName
			
        if (args.length > 0) {
            execute(args)
        } else {
            // Create a dialog for this tool to collect user-specified
            // tool parameters.
            sd = new ScriptDialog(pluginHost, descriptiveName, this)	
		
            // Specifying the help file will display the html help
            // file in the help pane. This file should be be located 
            // in the help directory and have the same name as the 
            // class, with an html extension.
            sd.setHelpFile(name)
		
            // Specifying the source file allows the 'view code' 
            // button on the tool dialog to be displayed.
            def pathSep = File.separator
            def scriptFile = pluginHost.getResourcesDirectory() + "plugins" + pathSep + "Scripts" + pathSep + name + ".groovy"
            sd.setSourceFile(scriptFile)
			
            // add some components to the dialog
            sd.addDialogMultiFile("Select the input Whitebox raster files", "Input Whitebox Raster Files:", "Whitebox Raster (*.dep), DEP")
			
            // resize the dialog to the standard size and display it
            sd.setSize(800, 400)
            sd.visible = true
        }
    }

    // The CompileStatic annotation can be used to significantly
    // improve the performance of a Groovy script to nearly 
    // that of native Java code.
    @CompileStatic
    private void execute(String[] args) {
        try {
			int i = 0;
        	double x, y
        	int progress = 0;
        	int oldProgress = -1;

        	if (args.length != 1) {
				pluginHost.showFeedback("Incorrect number of arguments given to tool.")
				return
			}
			// read the input parameters
			String inputFileString = args[0]
			if (inputFileString.isEmpty()) {
	            pluginHost.showFeedback("One or more of the input parameters have not been set properly.");
	            return;
	        }
			String[] inputFiles = inputFileString.split(";")
			int numFiles = inputFiles.length;

			String executabledir = pluginHost.getResourcesDirectory() + "plugins" + File.separatorChar;
			String executablestr = "./gospatial";
			
            for (i = 0; i < numFiles; i++) {
            	if (numFiles > 1) {
                	progressLabel = "Loop ${(i + 1)} of $numFiles:";
                }

                String arg1 = " -run=\"whitebox2geotiff\"";
				String arg2 = " -args=\"${inputFiles[i]};${inputFiles[i].replace(".dep", ".tif")}\"";
				String cmd = executablestr + arg1 + arg2

				executeOnShell(cmd, new File(executabledir));
			
            	// check to see if the user has requested a cancellation
				if (pluginHost.isRequestForOperationCancelSet()) {
					pluginHost.showFeedback("Operation cancelled")
					return
				}
                
            }

        } catch (OutOfMemoryError oe) {
            pluginHost.showFeedback("An out-of-memory error has occurred during operation.")
	    } catch (Exception e) {
	        pluginHost.showFeedback("An error has occurred during operation. See log file for details.")
	        pluginHost.logException("Error in " + descriptiveName, e)
        } finally {
        	// reset the progress bar
        	pluginHost.updateProgress(0)
        }
    }

    @Override
    public void actionPerformed(ActionEvent event) {
    	if (event.getActionCommand().equals("ok")) {
            final def args = sd.collectParameters()
            sd.dispose()
            final Runnable r = new Runnable() {
            	@Override
            	public void run() {
                    execute(args)
            	}
            }
            final Thread t = new Thread(r)
            t.start()
    	}
    }

	private int executeOnShell(String command, File workingDir) {
	  //println command
	  def process = new ProcessBuilder(addShellPrefix(command))
	                                    .directory(workingDir)
	                                    .redirectErrorStream(true) 
	                                    .start()
	  process.inputStream.eachLine {
	  		def str = ((String)it).trim()
	  		if (!str.isEmpty()) {
	  			if (str.contains("%")) {
	  				def strArray = str.split(" ")
	  				String label = str.replace(strArray[strArray.length-1], "")
	  				if (label.trim().equals("Progress:")) { label = progressLabel }
	  				int progress = Integer.parseInt(strArray[strArray.length-1].replace("%", "").trim())
					pluginHost.updateProgress(label, progress)
					
					// check to see if the user has requested a cancellation
					if (pluginHost.isRequestForOperationCancelSet()) {
						pluginHost.showFeedback("Operation cancelled")
						return
					} 				
		  		} else {
		  			println(str)
		  		}
	  		}
	  }
	  process.waitFor();
	  
	  return process.exitValue()
	}
	 
	private String[] addShellPrefix(String command) {
	  def commandArray = new String[3]
	  commandArray[0] = "sh"
	  commandArray[1] = "-c"
	  commandArray[2] = command
	  return commandArray
	}
}

if (args == null) {
    pluginHost.showFeedback("Plugin arguments not set.")
} else {
    def f = new ExportGeoTIFF(pluginHost, args, name, descriptiveName)
}
