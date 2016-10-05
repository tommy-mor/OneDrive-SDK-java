package org.onedrive.container.facet;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

/**
 * https://dev.onedrive.com/facets/sharepointIds_facet.htm
 * {@// TODO: Enhance javadoc}
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public class SharePointIdsFacet {
	@Getter protected final String siteId;
	@Getter protected final String webId;
	@Getter protected final String listId;
	@Getter protected final long listItemId;
	@Getter protected final String listItemUniqueId;

	@JsonCreator
	protected SharePointIdsFacet(@JsonProperty("siteId") String siteId,
								 @JsonProperty("webId") String webId,
								 @JsonProperty("listId") String listId,
								 @JsonProperty("listItemId") Long listItemId,
								 @JsonProperty("listItemUniqueId") String listItemUniqueId) {
		if (listItemId == null) {
			throw new RuntimeException("\"listItemId\" filed can not be null");
		}
		this.siteId = siteId;
		this.webId = webId;
		this.listId = listId;
		this.listItemId = listItemId;
		this.listItemUniqueId = listItemUniqueId;
	}
}