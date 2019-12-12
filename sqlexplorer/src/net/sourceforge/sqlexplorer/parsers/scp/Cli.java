package net.sourceforge.sqlexplorer.parsers.scp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import net.sourceforge.sqlexplorer.oracle.dbproduct.OracleQuery;
import net.sourceforge.sqlexplorer.oracle.dbproduct.OracleQueryParser;
import net.sourceforge.sqlexplorer.parsers.ParserException;
import net.sourceforge.sqlexplorer.parsers.Query;

/**
 * Runs preprocessing of SQL from a CLI, used to strip structured comments from code.
 * This works for Oracle only, but would translate easily enough into other platforms
 * (if anyone ever used it for anything else...)
 * 
 * Usage:
 *      Cli [options] file[...]
 *  
 * where options are: 
 *  --input-dir inputDir        The directory to load relative to 
 *  --output-dir outputDir      The base directory to output to
 *  --exec-file file            (optional) Filename of script to load all processed files
 * 
 */
public class Cli {
    
    private File inputDir;
    private File outputDir;
    private ArrayList<String> execNames = new ArrayList();
    
    public Cli(File inputDir, File outputDir) {
        super();
        this.inputDir = inputDir;
        this.outputDir = outputDir;
    }
    
    public void saveExecFile(File file) throws IOException {
        String logName = file.getName();
        int pos = logName.lastIndexOf('.');
        if (pos > -1)
            logName = logName.substring(0, pos) + ".log";
        else
            logName += ".log";
        
        String result = "spool " + logName + "\n" +
                "set termout off\n";
        String absDir = outputDir.getAbsolutePath();
        for (String name : execNames) {
            if (name.startsWith(absDir))
                name = name.substring(absDir.length() + 1);
            result += "prompt RUNNING: " + name + "\n";
            result += "@" + name + "\n";
        }
        result += "spool off\n" +
                "exit\n";
        writeFile(file, result);
    }
    
    public void convertFiles(ArrayList<String> filenames) throws IOException, ParserException {
        for (int i = 0; i < filenames.size(); i++) {
            String filename = filenames.get(i);
            File inFile = new File(inputDir, filename);
            
            if (!inFile.exists()) {
                System.err.println("Cannot find " + inFile.getAbsolutePath());
                continue;
            }
            System.out.println("Processing " + inFile);
            int pos = filename.lastIndexOf('.');
            if (pos > -1) {
                String ext = filename.substring(pos + 1);
                if (ext.equals("fls")) {
                    String str = readFile(inFile);
                    ArrayList<String> next = new ArrayList();
                    for (String line : str.split("\n")) {
                        line = line.trim();
                        if (line.length() > 0)
                            next.add(line);
                    }
                    convertFiles(next);
                    continue;
                }
            }
            File outFile = new File(outputDir, filename);
            
            convert(inFile, outFile);
        }
    }
    
    private void convert(File inFile, File outFile) throws IOException, ParserException {
        String str = readFile(inFile);
        OracleQueryParser qp = new OracleQueryParser(str, true);
        qp.parse();
        String result = "";
        for (Query _q : qp) {
            OracleQuery q = (OracleQuery)_q;
            if (q.getNamedParameters() != null && !q.getNamedParameters().isEmpty()) {
                System.out.println("Unexpected named parameters: " + q);
                continue;
            }
            q.stripComments();
            for (String line : q.getQuerySql().toString().split("\n")) {
                if (line.trim().length() > 0) {
                    result += line + "\n";
                }
            }
            result += "/\n\n";
        }
        writeFile(outFile, result);
        execNames.add(outFile.getPath());
    }

    private String readFile(File file) throws IOException {
        byte[] buffer = new byte[64 * 1024];
        String result = "";
        int len;
        FileInputStream is = new FileInputStream(file);
        while ((len = is.read(buffer)) > -1)
            result += new String(buffer, 0, len, "utf8");
        is.close();
        return result;
    }
    
    private void writeFile(File file, String data) throws IOException {
        file.getParentFile().mkdirs();
        FileWriter w = new FileWriter(file);
        w.write(data);
        w.close();
    }

    public static void main(String[] args) throws Exception {
        ArrayList<String> files = new ArrayList();
        File outputDir = null;
        File inputDir = null;
        File execFile = null;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--input-dir")) {
                i++;
                if (i < args.length)
                    inputDir = new File(args[i]);
                
            } else if (args[i].equals("--output-dir")) {
                i++;
                if (i < args.length)
                    outputDir = new File(args[i]);
                
            } else if (args[i].equals("--exec-file")) {
                i++;
                if (i < args.length)
                    execFile = new File(outputDir, args[i]);
                
            } else 
                files.add(args[i]);
        }
        
        if (inputDir == null || outputDir == null || files.isEmpty()) {
            System.out.println("Usage: Cli --input-dir inputDir --output-dir outputDir file[...]");
            return;
        }
        if (!inputDir.exists() || !inputDir.isDirectory()) {
            System.out.println("Cannot create/use input directory " + inputDir.getAbsolutePath());
            return;
        }
        outputDir.mkdirs();
        if (!outputDir.exists() || !outputDir.isDirectory()) {
            System.out.println("Cannot create/use output directory " + outputDir.getAbsolutePath());
            return;
        }

        Cli cli = new Cli(inputDir, outputDir);
        cli.convertFiles(files);
        if (execFile != null)
            cli.saveExecFile(execFile);
    }
    
}
