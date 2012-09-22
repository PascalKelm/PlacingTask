import java.awt.Component;
import java.io.BufferedReader;
import java.io.File;
import javax.swing.filechooser.FileFilter;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import javax.swing.JFileChooser;

public class ValidationPT {

	public static void main(String[] args) throws IOException
	{
		// local variables 
		String organisation = "";
		List<Run> list_Run = new ArrayList<Run>();
		
		// Default thresholds
	    String th = "1;10;20;50;100;200;500;1000;2000;5000;10000;15000;20000";
	    System.out.println("Valitation toolbox by Pascal Kelm for the Placing Task at MediaEval.");
	    System.out.println("required argument: pt2009/pt2010/pt2012");
	    System.out.println("optinal argument: semicolon-separated thresholds (default value: 1;10;20;50;100;200;500;1000;2000;5000;10000;15000;20000)");
	    System.out.println("");
	    System.out.println("");
	    
		if(args.length > 0)
		{
			// Optional argument for new thresholds
			if (args.length == 2)
			{
				th = args[1];
			}			
			
			if (args[0].contains("pt"))
			{
				/*********************
				 * Import ground truth
				 *********************/
				System.out.print("Import ground truth... ");
				
				// Read file
				String s = null; 
		        StringBuilder st_gt = new StringBuilder();
		        BufferedReader in = new BufferedReader(new FileReader("src/" + args[0] + ".txt")); 
		        List<Position> list_GroundTruth = new ArrayList<Position>();
		        while((s = in.readLine()) != null){ 
		        	String[] arg = s.split(";");
                    Position temp_Pos = new Position();
                    temp_Pos.FileName = Integer.parseInt(arg[0].trim().substring(0, 5));
                    temp_Pos.Latitude = Double.parseDouble(arg[1].trim());
                    temp_Pos.Longitude = Double.parseDouble(arg[2].trim()); 
                    list_GroundTruth.add(temp_Pos);
		        } 
		        in.close();		        
		        
		        System.out.println(" Done. ");
				System.gc();
								
				/*********************
				 *  Open file dialog for the *.Placing files
				 *********************/
				System.out.print("Open placing files...");
				String filename = File.separator+"tmp";
				JFileChooser fc = new JFileChooser(new File(filename));
				Component frame = null;
				fc.setDialogTitle("Open Placing Files");
				FileFilter filter1 = new ExtensionFileFilter("Placing Files", new String[] { "placing" });
		        fc.setFileFilter(filter1);
		        
		        if(fc.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION)
				{
		        	// Import 
		        	File selFile = fc.getSelectedFile();		        	
		        	in = new BufferedReader(new FileReader(selFile.getAbsoluteFile())); 
			        while((s = in.readLine()) != null){ 
			        	String[] s_args = s.trim().split(";");
	                    if (s_args.length == 5)
	                    {
	                    	Run temp_Run = new Run();
	                        temp_Run.RunName = s_args[0].trim();
	                        temp_Run.Organisation = s_args[1].trim();
	                        organisation = temp_Run.Organisation;
	                        
	                        temp_Run.FileName = Integer.parseInt(s_args[2].trim().substring(0, 5));
	                        temp_Run.Latitude = Double.parseDouble(s_args[3].trim().replace(",", "."));
	                        temp_Run.Longitude = Double.parseDouble(s_args[4].trim().replace(",", "."));
	                        
	                        Position pos_gt = findPosition(list_GroundTruth, temp_Run.FileName);
	                        if (pos_gt != null)
	                        {
	                            temp_Run.HavesineDistance = calc_HaversineDist(temp_Run, pos_gt);
	                            temp_Run.GroundTruth = pos_gt;
	                            list_Run.add(temp_Run);
	                        }
	                        else
	                        {
	                        	System.out.println("Could not find " + s_args[2].trim().substring(0, 5) + ".xml");
	                        }
	                    }
			        }		        	
		        	System.out.println(" Done for file \"" + selFile.getName() + "\".");
					System.gc();
					
					/*************
					 *  Sort Runs
					 *************/
					System.out.print("Sorting runs... ");
					List<String> run_name = new ArrayList<String>();
		            for (int i = 0; i < list_Run.size(); i++)
		            {
		                if(!run_name.contains(list_Run.get(i).RunName))
		                    run_name.add(list_Run.get(i).RunName);
		            }

		            List<List<Run>> list_All_Runs = new ArrayList<List<Run>>();
		            for (int j = 0; j < run_name.size(); j++)
		            {
		                List<Run> list_temp = new ArrayList<Run>();
		                for (int i = 0; i < list_Run.size(); i++)
		                {
		                    if (run_name.get(j).contains(list_Run.get(i).RunName))
		                        list_temp.add(list_Run.get(i));
		                }
		                list_All_Runs.add(list_temp);
		            }
		            System.out.println(" Done.");
					System.gc();
					
					/****************
					 *  Calc thresholds
					 ****************/
					System.out.print("Calculating thresholds... ");
		            String[] st_th = th.split(";");
		            List<Double> list_th = new ArrayList<Double>();
		            for (int i = 0; i < st_th.length; i++)
		            {
		                list_th.add(Double.parseDouble(st_th[i].trim()));
		            }
		            Double[] d_th = (Double[]) list_th.toArray(new Double[0]);
		            List<List<Rang>> list_range = new ArrayList<List<Rang>>();
		            for (int i = 0; i < d_th.length; i++)
		            {
		                List<Rang> list_range_temp = new ArrayList<Rang>();
		                for (int h = 0; h < list_All_Runs.size(); h++)
		                {
		                    int c = 0;
		                    for (int j = 0; j < list_All_Runs.get(h).size(); j++)
		                    {
		                        if (list_All_Runs.get(h).get(j).HavesineDistance < d_th[i])
		                        {
		                            c++;
		                        }
		                    }
		                    Rang temp_rang = new Rang();
		                    temp_rang.Count = c;
		                    temp_rang.Run = list_All_Runs.get(h).get(0).RunName;
		                    temp_rang.Threshold = d_th[i];
		                    list_range_temp.add(temp_rang);
		                }
		                list_range.add(list_range_temp);
		            }
		            System.out.println(" Done.");
					System.gc();
		            
		            /********************
		             * Find missing values
		             ********************/
					System.out.print("Finding missing values... ");
		            List<Rang> tList = new ArrayList<Rang>();
		            for (int h = 0; h < list_All_Runs.size(); h++)
		            {
		            	Rang temp_rang = new Rang();
		            	temp_rang.Threshold = -1;
		            	temp_rang.Count = list_GroundTruth.size() - list_All_Runs.get(h).size();
		            	temp_rang.Run = list_All_Runs.get(h).get(0).RunName;	
		                tList.add(temp_rang);
		            }
		            list_range.add(tList);
		            System.out.println(" Done.");
					System.gc();
					
					/*****************
					 * Show Results
					 *****************/
					System.out.println("");
					System.out.println("Threshold \t Run \t Count");
					for (int i = 0; i < d_th.length; i++) 
                    {
                     	for (int j = 0; j < list_range.get(i).size(); j++)
 	                    {
 	                    	Rang t_run = list_range.get(i).get(j);
 	                        System.out.println(t_run.Threshold + "\t" + t_run.Run + " \t" + t_run.Count);
 	                    }	
					}
                    for (int j = 0; j < tList.size(); j++)
	                {
	                    	Rang t_run = tList.get(j);
	                    	System.out.println("Missing \t" + t_run.Run + " \t" + t_run.Count);
	                        
	                }
					
					/*****************
					 * Save Results
					 *****************/
					// Open file dialog	
                    System.out.println("");
					System.out.print("Save runs...");
					fc = new JFileChooser(new File(filename));
					frame = null;
					fc.setDialogTitle("Select Folder to save the results of the runs.");
			        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			        if(fc.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION)
					{
			        	// Save runs in folder
			        	for (int h = 0; h < list_All_Runs.size(); h++)
		                {
			        		BufferedWriter bw = new BufferedWriter(new FileWriter(fc.getSelectedFile().getAbsolutePath() + "/" + organisation + "_Run" + h + "_Results.csv"));
			        		bw.write("File \t Latitude[GT] \t Longitude[GT] \t Latitude[" + organisation + "] \t Longitude[" + organisation + "] \t Distance");
			        		bw.newLine();
		                    for (int j = 0; j < list_All_Runs.get(h).size(); j++)
		                    {
		                    	Run t_run = list_All_Runs.get(h).get(j);
		                        bw.write(t_run.FileName + ".xml \t" + t_run.GroundTruth.Latitude + " \t" + t_run.GroundTruth.Longitude + " \t" + t_run.Latitude + " \t" + t_run.Longitude +  " \t" + t_run.HavesineDistance);
		                        bw.newLine();
		                    }
			        		bw.close();
		                }
			        	System.gc();
			        	BufferedWriter bw = new BufferedWriter(new FileWriter(fc.getSelectedFile().getAbsolutePath() + "/" + organisation + "_Summary_Results.csv"));
			        	bw.write("Threshold \t Run \t Count");
                        bw.newLine();
                        for (int i = 0; i < d_th.length; i++) 
                        {
                        	for (int j = 0; j < list_range.get(i).size(); j++)
    	                    {
    	                    	Rang t_run = list_range.get(i).get(j);
    	                        bw.write(t_run.Threshold + "\t" + t_run.Run + " \t" + t_run.Count);
    	                        bw.newLine();
    	                    }	
						}
                        for (int j = 0; j < tList.size(); j++)
	                    {
	                    	Rang t_run = tList.get(j);
	                        bw.write("Missing \t" + t_run.Run + " \t" + t_run.Count);
	                        bw.newLine();
	                    }
		        		bw.close();
			        	// Save summary in folder
			        	
			        	
			        	System.out.println(" Done.");
			        	System.gc();
					}
			        else
			        {
			        	System.out.println(" Abort by user.");
			        }                    
					
				}
		        else
		        {
		        	System.out.println(" No file selected");
		        }		        
			}
			else
			{
				System.out.println("Missing arguments [pt2010/pt2011/pt2012].");
			}			
		}
		else
		{
			System.out.println("Missing arguments [pt2010/pt2011/pt2012].");
		}
	}

	public static double calc_HaversineDist(Run pos1, Position pos2)
    {
        double R = 6371.0;
        double dLat = toRadian(pos2.Latitude - pos1.Latitude);
        double dLon = toRadian(pos2.Longitude - pos1.Longitude);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(toRadian(pos1.Latitude)) * Math.cos(toRadian(pos2.Latitude)) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.min(1, Math.sqrt(a)));
        double d = R * c;
        return d;
    }
	
	public static double toRadian(double val)
    {
        return (Math.PI / 180) * val;
    }
	
	private static Position findPosition(List<Position> list_GroundTruth, int name) {
		for (Position pos : list_GroundTruth) 
		{
			  if (pos.FileName == name) 
			  {
				  return pos; 
			  }
		}
		return null;
	}

}

class  Rang
{
    public double Threshold;
    public int Count;
    public String Run;
}

class Position
{
    public int FileName;
    public double Latitude;
    public double Longitude;
}

class Run
{
    public String RunName;
    public String Organisation;
    public int FileName;
    public double Latitude;
    public double Longitude;
    public double HavesineDistance;
    public Position GroundTruth;
}

class ExtensionFileFilter extends FileFilter {
	  String description;
	  String extensions[];

	  public ExtensionFileFilter(String description, String extension) {
	    this(description, new String[] { extension });
	  }

	  public ExtensionFileFilter(String description, String extensions[]) {
	    if (description == null) {
	      this.description = extensions[0];
	    } else {
	      this.description = description;
	    }
	    this.extensions = (String[]) extensions.clone();
	    toLower(this.extensions);
	  }

	  private void toLower(String array[]) {
	    for (int i = 0, n = array.length; i < n; i++) {
	      array[i] = array[i].toLowerCase();
	    }
	  }

	  public String getDescription() {
	    return description;
	  }

	  public boolean accept(File file) {
	    if (file.isDirectory()) {
	      return true;
	    } else {
	      String path = file.getAbsolutePath().toLowerCase();
	      for (int i = 0, n = extensions.length; i < n; i++) {
	        String extension = extensions[i];
	        if ((path.endsWith(extension) && (path.charAt(path.length() - extension.length() - 1)) == '.')) {
	          return true;
	        }
	      }
	    }
	    return false;
	  }
}

