package com.github.lyokofirelyte.PseudoCompiler4J;

import java.awt.Desktop;
import java.io.Console;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class PseudoCompiler {

    private static String[] text;
    private String[] correctedText;
    private String[] settings;

    private String version = "1.5";

    private static Map<String, String> setting = new HashMap<String, String>();
    private boolean debug = false;
    private static boolean allowSettings = false;
    private static boolean isInit = false;

    private String user = System.getProperty("user.name");
    private String toCompile = "";
    private String suggestions = "";

    public void modifySetting(String setting, String newValue)
    {
        PseudoCompiler.setting.put(setting, newValue);

        if (setting.equals("debugs"))
        {
            debug = Boolean.valueOf(newValue);
        }

        saveSettings();
    }

    public String getSetting(String setting)
    {
        return PseudoCompiler.setting.get(setting);
    }

    public boolean containsSetting(String setting)
    {
        return PseudoCompiler.setting.containsKey(setting);
    }

    public static void main(String[] args)
    {

    	try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e1) {
			e1.printStackTrace();
		}
    	
        if (!isInit)
        {
        	// handle saving settings on exit
        }

        File file = new File("settings.pseudo");
        allowSettings = file.exists();

        if (!allowSettings)
        {
            try
            {
            	file.createNewFile();
                allowSettings = true;
            }
            catch (Exception e)
            {
                allowSettings = false;
                System.out.println("Could not find settings file - using defaults.");
            }

            /* Default Settings */
            setting.put("debugs", "false");
            setting.put("update", "automatic");
            setting.put("author", "David Tossberg");
            setting.put("source", "Click to view!");
            saveSettings();
        }

        if (args.length == 0)
        {
            JFileChooser jfc = new JFileChooser();
            jfc.setCurrentDirectory(new File("settings.pseudo"));
            
            JFrame frame = new JFrame();
            frame.add(jfc);
            
            if (jfc.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION){
            	args = new String[] { jfc.getSelectedFile().getPath() };  
            }
            else
            {
                System.out.println("You must select a file to run. Reboot the program and choose a file.\n[ press enter to exit ]");
               	System.console().readLine();
                System.exit(0);
                return;
            }
        }

        if (args[0].endsWith(".txt"))
        {
            try {
				new PseudoCompiler().start(args[0]);
			} catch (Exception e) {
				e.printStackTrace();
			}
        }
        else
        {
            String weirdExtension = args[0].split(".")[args[0].split(".").length - 1];
            System.out.println("I have no idea what to do with a " + weirdExtension + " file.\nPlease drag in a .txt file.");
            System.console().readLine();
        }

        isInit = true;
    }

    static void onExit() // TODO
    {
        saveSettings();
    }

    private static void saveSettings()
    {
        if (setting.size() > 2)
        {
            String[] toSave = new String[setting.size()];
            int i = 0;

            for (String set : setting.keySet())
            {
                toSave[i] = set + "~" + setting.get(set);
                i++;
            }
            
            try {
                PrintWriter pw = new PrintWriter("settings.pseudo", "UTF-8");
                
                for (String thing : toSave){
                	pw.println(thing);
                }
                
                pw.close();
            } catch (Exception botchedSave){
            	botchedSave.printStackTrace();
            }
        }
    }

    private String awaitInput()
    {
        System.out.print("\n" + user + "@pseudo-root:~$ ");

        String input = System.console().readLine();

        if (debug)
        {
            writeLine(input, "input");
        }

        return input;
    }

    private void loadSettings()
    {
        for (String setting : settings)
        {
            String[] split = setting.split("~");
            PseudoCompiler.setting.put(split[0].toLowerCase(), split[1]);
            
            switch (split[0].toLowerCase())
            {
                case "debugs":

                    debug = Boolean.valueOf(split[1]);

                break;
                
                case "javalc":
                	
                	//System.setProperty("java.home", split[1]);
                	
                break;
            }
        }
    }

    public void start(String fileName) throws Exception
    {

        Object[] objList = Files.readAllLines(new File(fileName).toPath()).toArray();
        
        text = new String[objList.length];
        
        for (int i = 0; i < objList.length; i++){
        	text[i] = objList[i].toString() + "\n";
        }

        if (allowSettings)
        {
        	objList = Files.readAllLines(new File("settings.pseudo").toPath()).toArray();
            settings = new String[objList.length];
            
            for (int i = 0; i < objList.length; i++){
            	settings[i] = objList[i].toString();
            }
            
            loadSettings();
        }

        if (text.length < 2)
        {
            System.out.println("There isn't enough code in the file to run.");
            System.console().readLine();
            return;
        }
        
       /* if (!containsSetting("javalc") || getSetting("javalc").equals("none")){
        	JOptionPane.showConfirmDialog(null, "You must choose your JAVA JDK location in a moment.\nIf you don't know where it is, please see the instructions.txt.", "JAVA JDK?!", JOptionPane.OK_OPTION);
            JFileChooser jfc = new JFileChooser();
            jfc.setCurrentDirectory(new File("settings.pseudo"));
            
            JFrame frame = new JFrame();
            frame.add(jfc);
            
            if (jfc.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION){
            	System.setProperty("java.home", jfc.getSelectedFile().getParent());
            	modifySetting("javalc", jfc.getSelectedFile().getParent());
            	System.out.println(jfc.getSelectedFile().getParent());
            	JOptionPane.showConfirmDialog(null, "If you've choosen an invalid path you'll have to delete settings.pseudo and try again.", "Good luck!", JOptionPane.OK_OPTION);
            } else {
            	JOptionPane.showConfirmDialog(null, "I can't do anything until you choose your java JDK.", "You've made me sad.", JOptionPane.OK_OPTION);
            }
        }*/

        String name = fileName;
        suggestions = fileName.replace(fileName.split("/")[fileName.split("/").length - 1], "corrected.txt");
        correctedText = new String[text.length];

        List<String> parens = new ArrayList<String>(); // Things that need complete paren sets ( )
        parens.add("module");
        parens.add("function");
        parens.add("method");
        parens.add("call");

        for (int i = 0; i < text.length; i++)
        {
            text[i] = text[i].trim();

            if (parens.contains(text[i].split(" ")[0].toLowerCase()))
            {
                text[i] = fixParens(text[i]);
            }

            correctedText[i] = text[i];
        }

        PrintWriter pw = new PrintWriter(suggestions);
        
        for (String thing : correctedText){
        	pw.println(thing);
        }
       
        pw.close();

        for (String s : new String[]
        { 
            "protected int inputAsInteger(){",

                "System.out.println(\"> \");",

                "try { ",
                    "return Integer.parseInt(System.console().readLine()); ",
               " } catch (Exception e){ ",
                    "System.out.println(\"Can't convert input to an integer - using 0 instead!\"); ",
                    "return 0;",
                "}",
            "}",
            
            "protected float inputAsReal(){",

                "System.out.println(\"> \");",

                "try { ",
                    "return Float.parseFloat(System.console().readLine()); ",
               " } catch (Exception e){ ",
                    "System.out.println(\"Can't convert input to a real - using 0 instead!\"); ",
                    "return 0;",
                "}",
            "}",
            "protected boolean inputAsBoolean(){",

                "System.out.println(\"> \");",

                "try { ",
                    "return Boolean.valueOf(System.console().readLine()); ",
               " } catch (Exception e){ ",
                    "System.out.println(\"Can't convert input to a boolean - using false instead!\"); ",
                    "return false;",
                "}",
            "}",
            
            "protected boolean isInteger(String input){",

                "try { ",
                    "int test = Integer.parseInt(input); ",
                    "return true;",
               " } catch (Exception e){ ",
                    "return false; ",
                "}",
            "}",

            "protected boolean isReal(String input){",

                "try { ",
                    "float test = Float.parseFloat(input); ",
                    "return true;",
               " } catch (Exception e){ ",
                    "return false; ",
                "}",
            "}",

            "protected boolean isBoolean(String input){",

                "try { ",
                    "boolean test = Boolean.valueOf(input); ",
                    "return true;",
               " } catch (Exception e){ ",
                    "return false; ",
                "}",
            "}",

            "protected int length(String input){",
                "return input.length();",
            "}",

            "protected String toLower(String input){",
                "return input.toLowerCase();",
            "}",

            "protected String toUpper(String input){",
                "return input.toUpperCase();",
            "}",

            "%after%", // fill in converted code
        }){
           toCompile += s;
        }

        for (int i = 0; i < text.length; i++)
        {

            text[i] = text[i].replace("False", "false");
            text[i] = text[i].replace("True", "true");
            text[i] = text[i].replace(" Integer ", " int ");
            text[i] = text[i].replace(" integer ", " int ");
            text[i] = text[i].replace("Constant", "");
            text[i] = text[i].replace("constant", "");

            String[] args = text[i].split(" ");

            switch (args[0].toLowerCase())
            {
                case "module": case "begin": // Module changeName() --> private void changeName(){

                    text[i] = text[i].substring(args[0].length());
                    text[i] = "public void " + text[i] + "{";

                    if (args[1].toLowerCase().equals("main"))
                    {
                        text[i] = "public void main() {";
                    }
                  
                break;

                case "function": // Function String changeName() --> private String changeName(){

                    text[i] = text[i].substring(args[0].length());
                    String toLower = text[i].split(" ")[1];
                    text[i] = "public " + toLower.toLowerCase() + text[i].substring(toLower.length() + 1) + "{";
                    text[i] = text[i].replace(" integer ", " int ");
                    text[i] = text[i].replace(" real ", " float ");
                    text[i] = text[i].replace(" string ", " String ");
                    text[i] = text[i].replace(" bool ", " boolean ");

                break;

                case "end": // End Module --> }

                    text[i] = "}";

                break;

                case "call":

                    text[i] = text[i].substring(args[0].length()) + ";";

                break;

                case "sleep":
                	
                    text[i] = "try { java.lang.Thread.sleep(1000 *" + Integer.parseInt(args[1]) + "); } catch (Exception ee){}";

                break;

                case "else":

                    if (args.length >= 2 && args[1].equals("if"))
                    {
                        text[i] = "} " + text[i];
                        
                        if (!text[i].contains("("))
                        {
                            text[i] = text[i].substring(args[0].length());
                            text[i] = "if ((\"\"+" + text[i] + ")";
                        }
                        
                        // if ((""+5==7)
                    	text[i] = text[i].replace("==", ").equals(\"\"+");
                    	// if ((""+5).equals(""+7))

                        text[i] += "){";
                        text[i] = replaceOperators(text[i]);
                        text[i] = text[i].replace(" then", "");
                        text[i] = text[i].replace(" Then", "");
                    }
                    else
                    {
                        text[i] = "} else {";
                    }

                break;

                case "if":

                    if (!text[i].toLowerCase().startsWith("if ("))
                    {
                        text[i] = text[i].substring(args[0].length());
                        text[i] = "if ((\"\"+" + text[i] + ")";
                    }
                    
                	text[i] = text[i].replace("==", ").equals(\"\"+");

                    text[i] += "){";
                    text[i] = replaceOperators(text[i]);
                    text[i] = text[i].replace(" then", "");
                    text[i] = text[i].replace(" Then", "");

                break;

                case "set": case "declare":

                    if (args[0].toLowerCase().equals("declare"))
                    {
                        text[i] = args[1].toLowerCase();
                    }
                    else
                    {
                        text[i] = args[1];
                    }


                    for (int x = 2; x < args.length; x++)
                    {
                        if (!args[x].toLowerCase().equals("reference") && !args[x].toLowerCase().equals("ref"))
                        {
                            text[i] += " " + args[x];
                        }
                    }

                    text[i] = text[i].replace("integer", "int");
                    text[i] = text[i].replace("real", "float");
                    text[i] = text[i].replace("True", "true");
                    text[i] = text[i].replace("False", "false");
                    text[i] = text[i].replace(" string ", " String ");
                    text[i] = text[i].replace("string ", " String ");
                    
                    args = text[i].split(" ");

                    if (args[1].contains("]") && args[1].contains("["))
                    {
                        text[i] = args[0] + "[] " + args[1].substring(0, args[1].indexOf('[')) + " = new " + args[0] + "[" + args[1].substring(args[1].indexOf('['), args[1].indexOf(']')).replace("[", "").replace("]", "") + "]";
                    }

                    text[i] += ";";

                break;

                case "display":

                    text[i] = text[i].substring(args[0].length());
                    text[i] = "System.out.println((" + (debug ? "new Object(){}.getClass().getEnclosingMethod().getName() + ' ' + '>' + ' ' + " : "\"> \" + ") + text[i] + ").toString());";
                    text[i] = replaceCommas(text[i]);

                break;

                case "input":

                    String header = "> ";
                    text[i] = "System.out.print(" + '"' + header + '"' + ");" + args[1] + " = System.console().readLine();";

                break;

                case "while":

                    if (!text[i].toLowerCase().startsWith("while ("))
                    {
                        text[i] = text[i].replace(args[0], "while (") + ")";
                    }

                    text[i] += "{";
                    text[i] = replaceOperators(text[i]);

                break;

                case "do-while": // for people who are too lazy to condition their while loops correctly.

                    if (!text[i].startsWith("do-while ("))
                    {
                        text[i] = text[i].substring(args[0].length());
                        text[i] = "while (!doWhileBool" + i + " || " + text[i] + ")";
                    }
                    else
                    {
                        text[i] = text[i].replace(")", " || !doWhileBool" + i + ")");
                    }

                    text[i] += "{ doWhileBool" + i + " = true;";
                    text[i] = "boolean doWhileBool" + i + " = false;" + text[i];
                    text[i] = replaceOperators(text[i]);

                break;

                case "for": // So, the pseudo syntax is actually 87% more confusing than the normal syntax.

                    if (text[i].toLowerCase().contains(" to ")) // only change if pseudo
                    {
                        String firstVar = text[i].split(" ")[1];
                        text[i] = text[i].substring(args[0].length());
                        text[i] = "for (int " + text[i];
                        text[i] = text[i].replace(" to ", "; " + firstVar + " <= ");
                        text[i] = text[i].replace(" To ", "; " + firstVar + " <= ");
                        if (text[i].toLowerCase().contains(" step "))
                        {
                            text[i] = text[i].replace(" Step ", "; " + firstVar + " = (");
                            text[i] = text[i].replace(" step ", "; " + firstVar + " = (");
                            text[i] += ")){";
                        }
                        else
                        {
                            text[i] += "; " + firstVar + "++";
                            text[i] += "){";
                        }
                    }

                break;

                case "break": case "return":

                    if (!text[i].endsWith(";"))
                    {
                        text[i] += ";";
                    }

                    text[i] = replaceCommas(text[i]);

                break;

                case "select":

                    text[i] = text[i].substring(args[0].length());
                    text[i] = "switch (" + text[i] + "){";

                break;

                case "case":

                    text[i] = text[i].substring(args[0].length());
                    text[i] = "case" + text[i];

                break;

                case "//":

                    text[i] = "/*" + text[i] + "*/";

                break;

                case "open":

                   

                break;

                default:

                    if (text[i].trim().length() > 1 && !text[i].contains(";") && !text[i].startsWith("//") && !text[i].startsWith("*/"))
                    {
                        text[i] += ";";
                    }

                    if (text[i].startsWith("//"))
                    {
                        text[i] = "/*" + text[i] + "*/";
                    }

                break;
            }
        }

        String newText = "";

        for (String s : text)
        {
            newText += s;
        }

        toCompile = toCompile.replace("%after%", newText);
        toCompile = toCompile.replace("\n", "");

        String phrase = (fileName.split("/")[fileName.split("/").length - 1] + " is ready").toUpperCase();
        String arrows = "";

        writeLine("Hey " + System.getProperty("user.name") + ", welcome to the Pseudo Compiler.", "system");
        writeLine("View the examples on proper formatting to avoid errors.", "system");
        writeLine("Written in Java!\n", "system");
        
        for (int i = 0; i < phrase.length(); i++)
        {
            arrows += "=";
        }

        System.out.println(arrows);
        System.out.println(phrase);
        System.out.println(arrows + "\n");

        try
        {
            if (getSetting("update").toLowerCase().equals("automatic"))
            {
                writeLine("Checking for updates...", "system");

                URL url = new URL("https://github.com/lyokofirelyte/PseudoCompiler/blob/master/README.md");
                //ReadableByteChannel rbc = Channels.newChannel(url.openStream());
                Scanner scanner = new Scanner(url.openStream());
                List<String> temp = new ArrayList<String>();
                String content = "";
                
                while (scanner.hasNext()){
                	temp.add(scanner.nextLine());
                }
                
                for (String thing : temp){
                	if (thing.contains("Version")){
                		content = thing;
                		break;
                	}
                }
                
                String version = "";

                int location = content.indexOf("<p>Version: ") + 11;
                int z = 1;

                while ((location + z) < content.toCharArray().length)
                {
                    char c = content.toCharArray()[location + z];
                    if (c == '<')
                    {
                        break;
                    }
                    else
                    {
                        version += c;
                    }
                    z++;
                }

                if (!version.equals(this.version))
                {
                    writeLine("A new version (" + version + ") is released.", "system");
                    writeLine("You're only running version " + this.version + ".", "system");
                    writeLine("Automatically updating...", "system");
                    
                    url = new URL("https://github.com/lyokofirelyte/PseudoCompiler/blob/master/PseudoCompiler.exe?raw=true");
                    ReadableByteChannel rbc = Channels.newChannel(url.openStream());
                    FileOutputStream fos = new FileOutputStream("PseudoCompiler v" + version + ".exe");
                    fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                    
                    return;
                }
                else
                {
                    writeLine("You're up to date! Running version " + this.version, "system");
                }
            }

            System.out.println();
            writeLine("Type 'run' to run your pseudo code.", "system");
            writeLine("Type 'help' to see all commands.", "system");
            writeLine("Type 'debug' to see method names.", "system");
            System.out.println();

            writeLine("Reference variables do not work.", "system");
            writeLine("Need help? Type 'example' to download the demo!", "system");
            writeLine("You can even run the example.txt!", "system");
            
            System.out.println();
            writeLine("This is a special bare-bones version for Linux/Mac.", "system");
        } 
        catch (Exception e)
        {
            writeLine("Update check failed. No internet or github is down.", "error");
        }

        while (true)
        {
            String str = awaitInput();

            if (str.equals("exit"))
            {
                System.exit(0);
            }
            else
            {
                switch (str.split(" ")[0])
                {
                    case "open":

                        JFileChooser jfc = new JFileChooser();
                        jfc.setCurrentDirectory(new File("."));
                        
                        JFrame frame = new JFrame();
                        frame.add(jfc);
                        
                        if (jfc.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION){
                        	main(new String[]{ jfc.getSelectedFile().getPath() });
                            return;
                        }

                    break;

                    case "update":

                        modifySetting("update", getSetting("update").equals("automatic") ? "disabled" : "automatic");
                        writeLine("Update setting changed to: " + getSetting("update"), "system");

                    break;

                    case "reload":

                        main(new String[] { fileName });

                    return;

                    case "changelog":

						try {
							Desktop.getDesktop().browse(new URI("https://github.com/lyokofirelyte/PseudoCompiler/commits/master"));
						} catch (URISyntaxException e1) {
							e1.printStackTrace();
						}

                    break;

                    case "suggestions":

                        if (new File(suggestions).exists())
                        {
                            String[] txt = (String[]) Files.readAllLines(new File(suggestions).toPath()).toArray();
                            int i = 1; // I was tempted to leave this at 0, but alas...
                            boolean found = false;

                            for (String t : txt)
                            {
                                if (t.contains("[system]"))
                                {
                                    writeLine("Suggestion @ line " + i, "system");
                                    writeLine(t.replace("[system]", ""), "read");
                                    writeLine("... ... ...", "system");
                                    found = true;
                                }
                                i++;
                            }

                            if (!found)
                            {
                                writeLine("No suggestions found! Good job!", "system");
                            }
                            else
                            {
                            	Desktop.getDesktop().edit(new File(suggestions));
                            }
                        }
                        else
                        {
                            writeLine("The suggestions file appears to be missing.", "error");
                        }

                    break;

                    case "run":

                        GenUtils.compileAndRun(toCompile, "settings.pseudo");

                        writeLine("----", "system");
                        writeLine("Code finished. Type 'help' for a list of commands.", "system");
                        writeLine("Remember to view the suggestions if your code didn't work out well.", "system");

                    break;

                    case "debug":

                        debug = !debug;
                        setting.put("debugs", debug + "");
                        saveSettings();
                        main(new String[] { fileName });

                    return;

                    case "source":

						try {
							Desktop.getDesktop().browse(new URI("https://github.com/lyokofirelyte/PseudoCompiler"));
						} catch (URISyntaxException e1) {
							e1.printStackTrace();
						}

                    break;

                    case "example":

                        URL url = new URL("https://raw.githubusercontent.com/lyokofirelyte/PseudoCompiler/master/example.txt");
                        ReadableByteChannel rbc = Channels.newChannel(url.openStream());
                        FileOutputStream fos = new FileOutputStream("example.txt");
                        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                        
                    break;

                    case "help":

                    	for (String s : new String[] 
                        {
                            "> Pseudo Compiler Commands <",
                            "--- --- --- --- --- --- ---",
                            "",
                            "run (runs the program)",
                            "example (displays example pseudo code)",
                            "about (what even is this?)",
                            "reload (reloads the text file for a fresh start)",
                            "debug (toggles debug mode on / off)",
                            "open (open a new file to run)",
                            "suggestions (shows corrected code)",
                            "source (view source code on github)",
                            "changelog (view a poorly described list of changes)",
                            "exit (exits the program)",
                            "",
                            "--- --- --- --- --- --- ---",
                            "Most Windows CMD commands will also work, such as dir, ipconfig, ping, etc."
                        }){
                    		System.out.println(s);
                        }
                                
                    break;

                    case "about":

                        for (String s : new String[] 
                        {
                             "> Pseudo Compiler Information <",
                             "--- --- --- --- --- --- ---",
                             "",
                             "Greetings! I'm David. I wrote this thing.",
                             "The purpose of this program is to compile & run pseudo code.",
                             "Specifically, the code standards taught in ITS 140 @ Purdue.",
                             "I feel that if students can actually see the code run, it will help a lot.",
                             "They can fix syntax mistakes, get a better feel for how code operates, etc.",
                             "And, of course, it's always cool to see what you wrote actually do something.",
                             "--- --- ---",
                             "This program converts your pseudo code into Java code, compiles, and runs.",
                             "That means you can actually type RAW Java code into your pseudo code.",
                             "You'll need to call the entire path if you need a system library.",
                             "(Example: java.lang.Thread)",
                             "I only import java.io for basic pseudo stuff.",
                             "You have access to all the functions that come with the data types too!",
                             "You can do anything with this compiler. And I hope that you enjoy."
                        })
                        {
                        	System.out.println(s);
                        }

                    break;
                }
            }
        }
    }

    private String replaceOperators(String item)
    {
        item = item.replace("NOT", "!=");
        item = item.replace("OR", "||");
        item = item.replace("AND", "&&");

        item = item.replace("not", "!=");
        item = item.replace("or", "||");
        item = item.replace("and", "&&");

        return item;
    }

    private String replaceCommas(String item)
    {
        String newString = "";
        boolean isInQuotes = false;

        for (int x = 0; x < item.length(); x++)
        {
            char c = item.toCharArray()[x];

            if (!isInQuotes)
            {
                if (c == (','))
                {
                    c = '+';
                }
                else if (c == ('"'))
                {
                    isInQuotes = true;
                }
            }
            else
            {
                if (c == '"')
                {
                    isInQuotes = false;
                }
            }

            newString += c;
        }

        return newString;
    }

    private String fixParens(String line) // assumes there needs to be a method of some sort
    {
        String newLine = "";
        boolean foundStart = false;
        boolean foundEnd = false;

        if (!line.contains("(") && !line.contains(")"))
        {
            return line + "() // Added ending parens." + " [system]"; // easy quick check before the rough stuff.
        }

        if (line.indexOf(')') != line.lastIndexOf(')') || line.indexOf('(') != line.lastIndexOf('('))
        {
            return "// Invalid module name // " + line + " [system]"; // That'll teach em'
        }

        for (char c : line.toCharArray())
        {
            if (!foundStart && c != '(')
            {
                newLine += c == (')') ? "() // Added start param & trimmed out the rest [system]" : c; // All good so far, add crap to the line.
                if (c == ')')
                {
                    break;
                }
            }
            else
            {
                if (c == '(')
                {
                    foundStart = true; // Ok, we need params or a end ) now.
                    newLine += c;
                }
                else if (c == ')')
                {
                    newLine += (foundStart ? ")" : "() // Added start paren ( to match closing ) [system]"); // Close it off.
                    foundEnd = true;
                }
                else
                {
                    newLine += c;
                }
            }
        }

        if (!foundEnd && foundStart)
        {
            newLine += ") // Added ending paren" + " [system]"; // They're lazy so we fix it for them.
        }

        return newLine;
    }

    private void writeLine(String message, String module)
    {
        System.out.println((debug ? module + " > " : "> ") + message);
    }
}