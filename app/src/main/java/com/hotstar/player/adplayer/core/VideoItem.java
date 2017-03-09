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

package com.hotstar.player.adplayer.core;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import com.adobe.mediacore.MediaResource;
import com.adobe.mediacore.metadata.Metadata;
import com.adobe.mediacore.metadata.MetadataNode;
import com.hotstar.player.adplayer.AdVideoApplication;
import com.hotstar.player.adplayer.feeds.ContentRenditionInfo;
import com.hotstar.player.adplayer.feeds.IFeedItemAdapter;
import com.hotstar.player.adplayer.feeds.ContentRenditionInfo.ContentFormat;
import com.hotstar.player.adplayer.feeds.ContentRenditionInfo.ContentType;
import com.hotstar.player.adplayer.utils.SerializableNameValuePair;

/**
 * The VideoItem class is the Primetime reference implementation's representation of one video
 * content item. The VideoItem class is built using the FeedItemAdapter
 * interface as the input and it invokes the FeedItemAdapter's APIs to populate
 * its own data structure, which is then used by the PSDK
 * 
 * @author dshei
 * 
 */
public class VideoItem implements Serializable {
	private static final long serialVersionUID = 1L;
	private final String LOG_TAG = "[Player]::VideoItem";

	private final String id;
	private final String title;
	private final ContentType type;
	private final Stream stream;
	private final Thumbnail thumbnail;
	private final List<SerializableNameValuePair> properties = new ArrayList<SerializableNameValuePair>();

	private final ArrayList<OverlayAdItem> overlayAdItems = new ArrayList<OverlayAdItem>();

	/**
	 * Class constructor. The VideoItem class is built using the FeedItemAdapter
	 * interface as the input and invokes its APIs to populate its data
	 * structure. Using the IFeedItemAdapter as an input parameter exemplifies
	 * how any input feed format can be adapted to the data structures used by
	 * the PSDK
	 * 
	 * @param feedItemAdapter
	 * @throws Exception
	 */
	public VideoItem(IFeedItemAdapter feedItemAdapter) throws Exception {
		try {
			id = feedItemAdapter.getId();
			title = feedItemAdapter.getTitle();
			type = feedItemAdapter.getContentType();

			if (feedItemAdapter.getStreamMetadata() != null) {
				stream = new Stream(feedItemAdapter.getStreamMetadata());
			} else {
				stream = new Stream();
			}

			List<ContentRenditionInfo> renditions = feedItemAdapter
					.getContentRenditions();
			for (ContentRenditionInfo rendition : renditions) {
				if (rendition.getContentFormat() == ContentFormat.hls) {
					stream.addManifest(rendition.getUrl(), ManifestType.hls);
				}
			}

			thumbnail = new Thumbnail(
					feedItemAdapter.getStreamThumbnailSmall(),
					feedItemAdapter.getStreamThumbnailLarge());

			if (feedItemAdapter.getProperties() != null) {
				properties.addAll(feedItemAdapter.getProperties());
			}

			if (feedItemAdapter.getOverlayAdItems() != null) {
				overlayAdItems.addAll(feedItemAdapter.getOverlayAdItems());
			}

		} catch (Exception e) {
			AdVideoApplication.logger.e(LOG_TAG + "#VideoItem", e.getMessage());
			throw e;
		}
	}

	/**
	 * Returns id of video item
	 * 
	 * @return String - containing the id of video item
	 */
	public String getId() {
		return id;
	}

	/**
	 * Returns title of video item
	 * 
	 * @return String - containing the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Returns the type of video item
	 * 
	 * @return ContentType - see ContentType enum definition
	 */
	public ContentType getType() {
		return type;
	}

	/**
	 * Returns the stream object of the video item
	 * 
	 * @return Stream - class that contains information required to play the
	 *         actual stream, such as the manifest urls and ad metadata
	 */
	public Stream getStream() {
		return stream;
	}

	/**
	 * Returns thumbnail class of video item
	 * 
	 * @return Thumbnail - class that contains the url to display the video item
	 *         thumbnail
	 */
	public Thumbnail getThumbnail() {
		return thumbnail;
	}

	/**
	 * Returns the properties of the video item
	 * 
	 * @return List<SerializableNameValuePair> - list of categories and keywords
	 *         related to the video item that can enhance a user's experience
	 */
	public List<SerializableNameValuePair> getProperties() {
		return properties;
	}

	/**
	 * Converts the video item to a PSDK class MediaResource that the
	 * MediaPlayer plays
	 * 
	 * @return MediaResource
	 * @throws IllegalArgumentException
	 */
	public MediaResource toMediaResource() throws IllegalArgumentException {
		String url = null;
		MediaResource.Type itemType = null;
		List<Stream.Manifest> manifests = getStream().getManifests();
		if (manifests.size() > 0) {
			url = manifests.get(0).getUrl();
			String type = manifests.get(0).getType().toString()
					.toUpperCase(Locale.ROOT);
			itemType = MediaResource.Type.valueOf(type);
		}

		if (url != null && itemType != null) {
			return MediaResource.createFromUrl(url, stream.getMetadata());
		}

		return null;
	}

	/**
	 * Gets the url of manifest
	 * 
	 * @return String - url of manifest
	 */
	public String getUrl() {
		String url = null;
		List<Stream.Manifest> manifests = getStream().getManifests();
		if (manifests.size() > 0) {
			url = manifests.get(0).getUrl();
		}

		return url;
	}

	/**
	 * Gets the stream type of manifest
	 * 
	 * @return String - type of manifest
	 */
	public String getStreamType() {
		String type = null;
		List<Stream.Manifest> manifests = getStream().getManifests();
		if (manifests.size() > 0) {
			type = manifests.get(0).getType().toString()
					.toUpperCase(Locale.ROOT);
		}

		return type;
	}

	/**
	 * Returns the metadata object of stream
	 * 
	 * @return Metadata
	 */
	public Metadata getAdvertisingMetadata() {
		return stream.getMetadata();
	}

	public ArrayList<OverlayAdItem> getOverlayAdItems() {
		return overlayAdItems;
	}

	/**
	 * The Thumbnail class contains the url to display the video item thumbnail.
	 * The url is obtained by invoking the API of the corresponding
	 * IFeedItemAdapter instance.
	 * 
	 * @author dshei
	 * 
	 */
	public class Thumbnail implements Serializable {
		private static final long serialVersionUID = 1L;
		private final String smallThumbnailUrl;
		private final String largeThumbnailUrl;

		public Thumbnail(String smallThumbnailUrl, String largeThumbnailUrl) {
			this.smallThumbnailUrl = smallThumbnailUrl;
			this.largeThumbnailUrl = largeThumbnailUrl;
		}

		/**
		 * Getter method for small thumbnail url
		 * 
		 * @return String - containing the small thumbnail url
		 */
		public String getSmallThumbnailUrl() {
			return smallThumbnailUrl;
		}

		/**
		 * Getter method for large thumbnail url
		 * 
		 * @return String - containing the large thumbnail url
		 */
		public String getLargeThumbnailUrl() {
			return largeThumbnailUrl;
		}
	}

	/**
	 * The Stream class contains information to play the actual stream, and is
	 * populated by invoking the APIs of the corresponding IFeedItemAdapter
	 * instance. It is populated with information obtained from the list of
	 * ContentRenditionInfo objects and MetadataNode. It represents information
	 * obtained from the ContentRenditionInfo as a Manifest object and
	 * MetadataNode information as just an internal variable.
	 * 
	 * @author dshei
	 * 
	 */
	public class Stream implements Serializable {
		private static final long serialVersionUID = 1L;
		private final MetadataNode metadata;
		private final List<Manifest> manifests;

		public Stream() {
			this.manifests = new Vector<Manifest>();
			this.metadata = new MetadataNode();
		}

		public Stream(MetadataNode metadata) {
			this.manifests = new Vector<Manifest>();
			this.metadata = metadata;
		}

		/**
		 * Returns the metadata object of stream
		 * 
		 * @return Metadata - this is the same Metadata object that is obtained
		 *         from the IFeedItemAdapter's getStreamMetadata() API and is
		 *         the Metadata object containing the key/value pair pointing to
		 *         a specific type of metadata which can be for Auditude Ads,
		 *         Direct Ad Breaks, Custom Ad Markers, or Custom Ad Provider
		 */
		public Metadata getMetadata() {
			return metadata;
		}

		/**
		 * Returns the list of manifest objects
		 * 
		 * @return List<Manifest> - list of Manifest objects
		 */
		public List<Manifest> getManifests() {
			return manifests;
		}

		/**
		 * Adds manifest object to list of manifests
		 * 
		 * @param url
		 *            - manifest url
		 * @param type
		 *            - see ManifestType enum definition
		 */
		protected void addManifest(String url, ManifestType type) {
			Manifest manifest = new Manifest(url, type);
			manifests.add(manifest);
		}

		/**
		 * The Manifest represents a rendition of the video item content, which
		 * can be alternate content, camera angles, etc
		 * 
		 * @author dshei
		 * 
		 */
		public class Manifest implements Serializable {
			private static final long serialVersionUID = 1L;
			private final ManifestType type;
			private final String url;

			/**
			 * Class constructor.
			 * 
			 * @param url
			 *            - of video content
			 * @param type
			 *            - see ManifestType enum definition
			 */
			public Manifest(String url, ManifestType type) {
				this.url = url;
				this.type = type;
			}

			/**
			 * Returns the ManifestType
			 * 
			 * @return ManifestType - see ManifestType enum definition
			 */
			public ManifestType getType() {
				return type;
			}

			/**
			 * Returns the manifest url
			 * 
			 * @return String - containing the manifest url
			 */
			public String getUrl() {
				return url;
			}
		}
	}

	public enum ManifestType {
		hls, hds
	}
}
