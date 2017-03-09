/*******************************************************************************
 * ADOBE CONFIDENTIAL
 *  ___________________
 *
 *  Copyright 2014 Adobe Systems Incorporated
 *  All Rights Reserved.
 *
 *  NOTICE:  All information contained herein is, and remains
 *  the property of Adobe Systems Incorporated and its suppliers,
 *  if any.  The intellectual and technical concepts contained
 *  herein are proprietary to Adobe Systems Incorporated and its
 *  suppliers and are protected by trade secret or copyright law.
 *  Dissemination of this information or reproduction of this material
 *  is strictly forbidden unless prior written permission is obtained
 *  from Adobe Systems Incorporated.
 ******************************************************************************/

package com.hotstar.player.adplayer.entitlement;

import com.adobe.adobepass.accessenabler.utils.StringEscapeUtils;
import com.adobe.mediacore.metadata.MetadataNode;
import com.adobe.mediacore.utils.StringUtils;
import com.hotstar.player.adplayer.AdVideoApplication;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringReader;

public class EntitlementMetadata extends MetadataNode
{
    public static final String ENTITLEMENT_METADATA = "entitlement_metadata";
    public static final String RESOURCE_ID_KEY = "entitlement_resource_id_metadata_key";

    private static final String LOG_TAG = AdVideoApplication.LOG_APP_NAME + "EntitlementMetadata";

    private String resourceId;

    /**
     * Check if the Entitlement Metadata contains a resource ID.
     * @return true if the metadata contains a resource ID, false if there is no resource id
     */
    public final boolean hasResourceId()
    {
        return !"".equals(getResourceId());
    }

    /**
     * Retrieve the Entitlement resource id. If no resource id is given in the metadata, an empty string is returned.
     * Any HTML characters are unescaped before returning the resource ID.
     * It is recommended to call {@link EntitlementMetadata#hasResourceId()} prior to calling this method.
     * @return a resource id or an empty string is no resource id was given in the metadata.
     */
    public final String getResourceId()
    {
        if (resourceId == null)
        {
            String id = getValue(RESOURCE_ID_KEY);

            if (StringUtils.isEmpty(id))
            {
                resourceId = "";
            }
            else
            {
                resourceId = StringEscapeUtils.unescapeHtml(id);
            }
        }

        return resourceId;
    }

    /**
     * Retrieve the Entitlement channel title from the resource id. If the resource id is a valid mRSS string, then
     * the channel title is taken from the <code>/rss/channel/title</code> element. If the resource id is not
     * a valid mRSS string, then this method returns the same as {@link EntitlementMetadata#getResourceId()}.
     * It is recommended to call {@link EntitlementMetadata#hasResourceId()} prior to calling this method.
     * @return the channel title from the resource id for this entitlement metadata node
     */
    public final String getChannelTitle()
    {
        String id = getResourceId();

        // fast fail if id is empty
        if ("".equals(id))
        {
            return id;
        }

        // first, assume resource is a XML mRSS string and attempt to parse
        try
        {
            // build XML document factory
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(id));

            Document document = builder.parse(is);

            // search specifically for the RSS channel title
            String expression = "/rss/channel/title/text()";

            // build XPath expression
            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xPath = xPathFactory.newXPath();
            XPathExpression xPathExpression = xPath.compile(expression);

            String title = (String) xPathExpression.evaluate(document, XPathConstants.STRING);
            return title;

        }
        catch (ParserConfigurationException e)
        {
            AdVideoApplication.logger.w(LOG_TAG + "#getChannelTitle", "Exception creating XML parser. " + e.getMessage());
        }
        catch (SAXException e)
        {
            AdVideoApplication.logger.d(LOG_TAG + "#getChannelTitle", "Resource is is not XML, parsing as plain text.");
        }
        catch (IOException e)
        {
            AdVideoApplication.logger.w(LOG_TAG + "#getChannelTitle", "IOException parsing resource id as XML.", e);
        }
        catch (XPathExpressionException e)
        {
            AdVideoApplication.logger.w(LOG_TAG + "#getChannelTitle", "XPathExpressionException parsing resource id as XML.", e);
        }

        // if we get here, parsing resource id as XML failed
        // assume resource id is plain text and return
        return id;
    }

}
