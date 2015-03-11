/* Copyright (c) 2001-2013, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.convert;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RawRGBInformation {
	
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/convert/RawRGBInformation.java,v 1.3 2015/01/03 23:01:25 dclunie Exp $";

	int imageWidthInPixels;
	int imageHeightInPixels;
	double pixelWidthInMillimetres;
	double pixelHeightInMillimetres;
	double pixelSeparationInMillimetres;
	String photometricInterpretation;
	int bitsPerPixel;
	String planarConfiguration;

	public RawRGBInformation(String inputFileName) throws IOException, NumberFormatException {
		BufferedReader r = new BufferedReader(new FileReader(inputFileName));

		Pattern pFrameSize = Pattern.compile("Frame size:[ \t]*([0-9]+),[ \t]*([0-9]+).*");
		Pattern pPixelSize = Pattern.compile("Pixel size:[ \t]*([0-9.]+)[ \t]*mm,[ \t]*([0-9.]+)[ \t]*mm,[ \t]*([0-9.]+)[ \t]*mm.*");
		Pattern pImageFormat = Pattern.compile("Image format:[ \t]*(RGB)[ \t]*([0-9]+) BIT[ \t]*(NON INTERLEAVED|INTERLEAVED).*");

		{
			String line = null;
			while ((line=r.readLine()) != null) {
				//line=line.toUpperCase(java.util.Locale.US);
//System.err.println(line);
				{
					Matcher mFrameSize = pFrameSize.matcher(line);
					if (mFrameSize.matches()) {
//System.err.println("matches Frame size");
						int groupCount = mFrameSize.groupCount();
//System.err.println("groupCount = "+groupCount);
						if (groupCount == 2) {
							imageWidthInPixels = Integer.parseInt(mFrameSize.group(1));
System.err.println("imageWidthInPixels = "+imageWidthInPixels);
							imageHeightInPixels = Integer.parseInt(mFrameSize.group(2));
System.err.println("imageHeightInPixels = "+imageHeightInPixels);
						}
						continue;
					}
				}
				{
					Matcher mPixelSize = pPixelSize.matcher(line);
					if (mPixelSize.matches()) {
//System.err.println("matches Pixel size");
						int groupCount = mPixelSize.groupCount();
//System.err.println("groupCount = "+groupCount);
						if (groupCount == 3) {
							pixelWidthInMillimetres = Double.parseDouble(mPixelSize.group(1));
System.err.println("pixelWidthInMillimetres = "+pixelWidthInMillimetres);
							pixelHeightInMillimetres = Double.parseDouble(mPixelSize.group(2));
System.err.println("pixelHeightInMillimetres = "+pixelHeightInMillimetres);
							pixelSeparationInMillimetres = Double.parseDouble(mPixelSize.group(3));
System.err.println("pixelSeparationInMillimetres = "+pixelSeparationInMillimetres);
						}
						continue;
					}
				}
				{
					Matcher mImageFormat = pImageFormat.matcher(line);
					if (mImageFormat.matches()) {
//System.err.println("matches Image Format");
						int groupCount = mImageFormat.groupCount();
//System.err.println("groupCount = "+groupCount);
						if (groupCount == 3) {
							photometricInterpretation = mImageFormat.group(1);
System.err.println("photometricInterpretation = "+photometricInterpretation);
							bitsPerPixel = Integer.parseInt(mImageFormat.group(2));
System.err.println("bitsPerPixel = "+bitsPerPixel);
							planarConfiguration = mImageFormat.group(3);
System.err.println("planarConfiguration = "+planarConfiguration);
						}
						continue;
					}
				}
			}
		}
	}

	/**
	 * <p>Read a Visible Human README file and extract the information describing the raw RGB image.</p>
	 *
	 * @param	arg	the inputFile
	 */
	public static void main(String arg[]) {
		try {
			if (arg.length == 1) {
				new RawRGBInformation(arg[0]);
			}
			else {
				System.err.println("Error: Incorrect number of arguments");
				System.err.println("Usage: RawRGBInformation inputFile");
				System.exit(1);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}

