/*
 * ************************************************************************
 *
 * ADOBE SYSTEMS INCORPORATED
 * Copyright 2013 Adobe Systems Incorporated
 * All Rights Reserved.
 * NOTICE:  Adobe permits you to use, modify, and distribute this file in accordance with the 
 * terms of the Adobe license agreement accompanying it.  If you have received this file from a 
 * source other than Adobe, then your use, modification, or distribution of it requires the prior 
 * written permission of Adobe.
 *
 **************************************************************************
 */

package com.hotstar.player.adplayer.feeds;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class XMLParserHelper {

	public static XmlPullParser nextElement(XmlPullParser xmlParser,
			String elementName) throws XmlPullParserException, IOException {
		if (xmlParser == null) {
			return null;
		}

		while (xmlParser.next() != XmlPullParser.END_TAG) {
			if (xmlParser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}

			if (xmlParser.getName().equals(elementName)) {
				return xmlParser;
			} else {
				skip(xmlParser);
			}
		}

		return null;
	}

	public static void skip(XmlPullParser xmlParser)
			throws XmlPullParserException, IOException {
		if (xmlParser.getEventType() != XmlPullParser.START_TAG) {
			throw new IllegalStateException();
		}
		int depth = 1;
		while (depth != 0) {
			switch (xmlParser.next()) {
			case XmlPullParser.END_TAG:
				depth--;
				break;
			case XmlPullParser.START_TAG:
				depth++;
				break;
			}
		}
	}

	public static String readElementText(XmlPullParser xmlParser,
			String elementName) throws XmlPullParserException, IOException {
		if (xmlParser == null) {
			return null;
		}

		xmlParser.require(XmlPullParser.START_TAG, null, elementName);

		String result = "";
		if (xmlParser.next() == XmlPullParser.TEXT) {
			result = xmlParser.getText();
			xmlParser.nextTag();
		}

		xmlParser.require(XmlPullParser.END_TAG, null, elementName);
		return result;
	}

}
