package org.onedrive.container.items;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.istack.internal.Nullable;
import lombok.Getter;
import org.network.BadRequestException;
import org.network.HttpsRequest;
import org.network.HttpsResponse;
import org.onedrive.Client;
import org.onedrive.container.BaseContainer;
import org.onedrive.container.IdentitySet;
import org.onedrive.container.facet.*;
import org.onedrive.utils.OneDriveRequest;

import java.io.IOException;
import java.time.ZonedDateTime;

/**
 * https://dev.onedrive.com/resources/item.htm
 * {@// TODO: enhance javadoc}
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
abstract public class BaseItem extends BaseContainer {
	@Getter protected String id;
	@Getter protected IdentitySet createdBy;
	@Getter protected ZonedDateTime createdDateTime;
	/**
	 * The {@code cTag} value is modified when content or metadata of any descendant of the folder is changed.
	 */
	@Getter protected String cTag;
	@Getter protected boolean deleted;
	@Getter protected String description;
	/**
	 * The {@code eTag} value is only modified when the folder's properties are changed, except for properties that are
	 * derived from descendants (like {@code childCount} or {@code lastModifiedDateTime}).
	 */
	@Getter protected String eTag;
	@Getter protected FileSystemInfoFacet fileSystemInfo;
	@Getter protected IdentitySet lastModifiedBy;
	@Getter protected ZonedDateTime lastModifiedDateTime;
	@Getter protected String name;
	@Getter protected ItemReference parentReference;
	@Getter @Nullable protected RemoteItemFacet remoteItem;
	@Getter @Nullable protected SearchResultFacet searchResult;
	@Getter protected SharedFacet shared;
	@Getter @Nullable protected SharePointIdsFacet sharePointIds;
	@Getter protected long size;
	@Getter protected String webDavUrl;
	@Getter protected String webUrl;
	protected Client client;

	public boolean isRemote() {
		return remoteItem != null;
	}

	public void delete() throws IOException {
		HttpsResponse response = OneDriveRequest.doDelete("/drive/items/" + id, client.getAccessToken());

		if (response.getCode() != 200) {
			throw new BadRequestException("Bad request. It must be already deleted item or wrong ID.");
		}
	}

	public static class ItemDeserializer extends JsonDeserializer<BaseItem> {
		private final Client client;

		public ItemDeserializer(Client client) {
			super();
			this.client = client;
		}

		@Override
		public BaseItem deserialize(JsonParser parser, DeserializationContext context) throws IOException {
			ObjectMapper codec = (ObjectMapper) parser.getCodec();
			ObjectNode node = codec.readTree(parser);

			BaseItem ret;

			if (node.has("file")) {
				if (node.has("folder") || node.has("package")) {
					throw new RuntimeException(HttpsRequest.NETWORK_ERR_MSG + " Duplicated type.");
				}
				ret = codec.convertValue(node, FileItem.class);
				ret.client = client;
			}
			else if (node.has("folder")) {
				if (node.has("file") || node.has("package")) {
					throw new RuntimeException(HttpsRequest.NETWORK_ERR_MSG + " Duplicated type.");
				}
				ret = codec.convertValue(node, FolderItem.class);
			}
			else if (node.has("package")) {
				if (node.has("folder") || node.has("file")) {
					throw new RuntimeException(HttpsRequest.NETWORK_ERR_MSG + " Duplicated type.");
				}
				ret = codec.convertValue(node, PackageItem.class);
				ret.client = client;
			}
			else {
				throw new RuntimeException(HttpsRequest.NETWORK_ERR_MSG);
			}

			return ret;
		}
	}
}