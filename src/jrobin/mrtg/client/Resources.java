/* ============================================================
 * JRobin : Pure java implementation of RRDTool's functionality
 * ============================================================
 *
 * Project Info:  http://www.sourceforge.net/projects/jrobin
 * Project Lead:  Sasa Markovic (saxon@eunet.yu);
 *
 * (C) Copyright 2003, by Sasa Markovic.
 *
 * Developers:    Sasa Markovic (saxon@jrobin.org)
 *                Arne Vandamme (cobralord@jrobin.org)
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 */
package jrobin.mrtg.client;

import jrobin.mrtg.MrtgException;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

class Resources {
	private static boolean runningFromJar() {
		String className = Resources.class.getName().replace('.', '/');
		String classJar = Util.class.getResource("/" + className + ".class").toString();
		return classJar.startsWith("jar:");
	}

	private static String getJarPath() {
		if (runningFromJar()) {
			return System.getProperty("java.class.path");
		}
		return null;
	}

	static byte[] getResource(String path) throws MrtgException {
		try {
			if (runningFromJar()) {
				// extract from jar
				String jarPath = getJarPath();
				JarResources jarResources = new JarResources(jarPath);
				return jarResources.getResource(path);
			} else {
				ByteArrayOutputStream outStream = new ByteArrayOutputStream();
				FileInputStream inStream = new FileInputStream(path);
				int b;
				while((b = inStream.read()) != -1) {
					outStream.write(b);
				}
				byte[] resource = outStream.toByteArray();
				inStream.close();
				outStream.close();
				return resource;
			}
		} catch (IOException e) {
			throw new MrtgException(e);
		}
	}

	static String getString(String path) throws MrtgException {
		byte[] stringBytes = getResource(path);
		return new String(stringBytes);
	}

	static ImageIcon getImageIcon(String path) throws MrtgException {
		byte[] imageBytes = getResource(path);
		return new ImageIcon(imageBytes);
	}

	static Image getImage(String path) throws MrtgException {
		ImageIcon imageIcon = getImageIcon(path);
		return imageIcon.getImage();
	}
}

class JarResources {

	// jar resource mapping tables
	private Hashtable htSizes = new Hashtable();
	private Hashtable htJarContents = new Hashtable();

	// a jar file
	private String jarFileName;

	JarResources(String jarFileName) throws IOException {
		this.jarFileName = jarFileName;
		init();
	}

	byte[] getResource(String name) {
		return (byte[]) htJarContents.get(name);
	}

	private void init() throws IOException {
		// extracts just sizes only.
		ZipFile zf = new ZipFile(jarFileName);
		Enumeration e = zf.entries();
		while (e.hasMoreElements()) {
			ZipEntry ze = (ZipEntry) e.nextElement();
			htSizes.put(ze.getName(), new Integer((int) ze.getSize()));
		}
		zf.close();
		// extract resources and put them into the hashtable.
		FileInputStream fis = new FileInputStream(jarFileName);
		BufferedInputStream bis = new BufferedInputStream(fis);
		ZipInputStream zis = new ZipInputStream(bis);
		ZipEntry ze = null;
		while ((ze = zis.getNextEntry()) != null) {
			if (ze.isDirectory()) {
				continue;
			}
			int size = (int) ze.getSize();
			// -1 means unknown size.
			if (size == -1) {
				size = ((Integer) htSizes.get(ze.getName())).intValue();
			}
			byte[] b = new byte[size];
			int rb = 0;
			int chunk = 0;
			while (size - rb > 0) {
				chunk = zis.read(b, rb, size - rb);
				if (chunk == -1) {
					break;
				}
				rb += chunk;
			}
			// add to internal resource hashtable
			htJarContents.put(ze.getName(), b);
		}
	}

}

