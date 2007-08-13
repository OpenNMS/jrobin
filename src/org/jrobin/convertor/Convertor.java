package org.jrobin.convertor;

import org.jrobin.core.RrdDb;
import org.jrobin.core.RrdException;

import java.io.*;

class Convertor {
	static final String SUFFIX = ".jrb";
	static final String SEPARATOR = System.getProperty("file.separator");
	static final Runtime RUNTIME = Runtime.getRuntime();

	private String rrdtoolBinary;
	private String workingDirectory;
	private String suffix;

	private int okCount, badCount;

	private Convertor(String rrdtoolBinary, String workingDirectory, String suffix) {
		this.rrdtoolBinary = rrdtoolBinary;
		this.workingDirectory = workingDirectory;
		this.suffix = suffix;
	}

	private void convert() {
		println("Converting RRDTool files to JRobin native format");
		println("Converted files will be placed in the same directory, with " +
			suffix + " suffix appended");
		println("==========================================");
		long start = System.currentTimeMillis();
		if(!workingDirectory.endsWith(SEPARATOR)) {
			workingDirectory += SEPARATOR;
		}
        File parent = new File(workingDirectory);
		if(parent.isDirectory() && parent.exists()) {
			// directory
			FileFilter filter = new FileFilter() {
				public boolean accept(File f) {
					try {
						return !f.isDirectory() && f.getCanonicalPath().endsWith(".rrd");
					} catch (IOException e) {
						return false;
					}
				}
			};
			File[] files = parent.listFiles(filter);
			for(int i = 0; i < files.length; i++) {
				print("[" + i + "/" + files.length + "] ");
				convertFile(files[i]);
			}
		}
		else if(!parent.isDirectory() && parent.exists()) {
			// single file
			convertFile(parent);
		}
		else {
			println("Nothing to do");
		}
		println("Conversion finished, " + okCount + " files ok, " + badCount + " files bad");
		long secs = (System.currentTimeMillis() - start + 500L) / 1000L;
		long mins = secs / 60;
		secs %= 60;
		println("Time elapsed: " + mins + ":" +	((secs < 10)? "0": "") + secs);
	}

	private long convertFile(File rrdFile) {
		long start = System.currentTimeMillis();
		String xmlPath = null, destPath = null;
		try {
			String sourcePath = rrdFile.getCanonicalPath();
			xmlPath = sourcePath + ".xml";
			destPath = sourcePath + suffix;
			print(rrdFile.getName() + " ");
			xmlDump(sourcePath, xmlPath);
			RrdDb rrd = new RrdDb(destPath, xmlPath);
			rrd.close();
			rrd = null;
			System.gc();
			okCount++;
			long elapsed = System.currentTimeMillis() - start;
			println("[OK, " + (elapsed / 1000.0) + "]");
			return elapsed;
		} catch (IOException e) {
			removeFile(destPath);
			badCount++;
			println("[IO ERROR]");
			return -1;
		} catch (RrdException e) {
			removeFile(destPath);
			badCount++;
			println("[RRD ERROR]");
			return -2;
		}
		finally {
			removeFile(xmlPath);
		}
	}

	private static boolean removeFile(String filePath) {
		if(filePath != null) {
			return new File(filePath).delete();
		}
		return true;
	}

	private void xmlDump(String sourcePath, String xmlPath) throws IOException {
		String[] cmd = new String[] { rrdtoolBinary, "dump", sourcePath };
		Process p = RUNTIME.exec(cmd);
		OutputStream outStream = new BufferedOutputStream(new FileOutputStream(xmlPath, false));
		transportStream(p.getInputStream(), outStream);
		transportStream(p.getErrorStream(), null);
		try {
			p.waitFor();
		}
		catch(InterruptedException ie) {
			// NOP
		}
	}

	public static void main(String[] args) {
		if(args.length < 2 || args.length > 3) {
			println("Usage: java -jar convertor.jar " +
				"<path to RRDTool binary> <RRD directory/file path> [converted file suffix]");
		}
		else {
			Convertor c = new Convertor(args[0], args[1], args.length == 3? args[2]: SUFFIX);
			c.convert();
		}
	}

	private final static void println(String msg) {
		System.out.println(msg);
	}

	private final static void print(String msg) {
		System.out.print(msg);
	}

	private static void transportStream(InputStream in, OutputStream out) throws IOException {
		try {
			int b;
			while((b = in.read()) != -1) {
				if(out != null) {
					out.write(b);
				}
			}
		}
		finally {
			in.close();
			if(out != null) {
				out.flush();
				out.close();
			}
		}
	}
}
