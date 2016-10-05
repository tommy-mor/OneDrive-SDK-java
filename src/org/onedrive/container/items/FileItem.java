package org.onedrive.container.items;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import lombok.Getter;
import org.network.HttpsResponse;
import org.onedrive.container.BaseContainer;
import org.onedrive.container.IdentitySet;
import org.onedrive.container.facet.*;
import org.onedrive.utils.OneDriveRequest;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.*;

/**
 * {@// TODO: Enhance javadoc}
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
@JsonDeserialize(as = FileItem.class)
public class FileItem extends BaseItem {
	@Getter @Nullable protected AudioFacet audio;
	@NotNull protected FileFacet file;
	@Getter @Nullable protected ImageFacet image;
	@Getter @Nullable protected LocationFacet location;
	@Getter @Nullable protected PhotoFacet photo;
	@Getter @Nullable protected VideoFacet video;

	@JsonCreator
	protected FileItem(@JsonProperty("id") String id,
					   @JsonProperty("audio") AudioFacet audio,
					   @JsonProperty("createdBy") IdentitySet createdBy,
					   @JsonProperty("createdDateTime") String createdDateTime,
					   @JsonProperty("cTag") String cTag,
					   @JsonProperty("deleted") boolean deleted,
					   @JsonProperty("description") String description,
					   @JsonProperty("eTag") String eTag,
					   @JsonProperty("file") FileFacet file,
					   @JsonProperty("fileSystemInfo") FileSystemInfoFacet fileSystemInfo,
					   @JsonProperty("image") ImageFacet image,
					   @JsonProperty("lastModifiedBy") IdentitySet lastModifiedBy,
					   @JsonProperty("lastModifiedDateTime") String lastModifiedDateTime,
					   @JsonProperty("location") LocationFacet location,
					   @JsonProperty("name") String name,
					   @JsonProperty("parentReference") ItemReference parentReference,
					   @JsonProperty("photo") PhotoFacet photo,
					   @JsonProperty("remoteItem") RemoteItemFacet remoteItem,
					   @JsonProperty("searchResult") SearchResultFacet searchResult,
					   @JsonProperty("shared") SharedFacet shared,
					   @JsonProperty("sharePointIds") SharePointIdsFacet sharePointIds,
					   @JsonProperty("size") long size,
					   @JsonProperty("video") VideoFacet video,
					   @JsonProperty("webDavUrl") String webDavUrl,
					   @JsonProperty("webUrl") String webUrl) {
		this.id = id;
		this.audio = audio;
		this.createdBy = createdBy;
		this.createdDateTime = BaseContainer.parseDateTime(createdDateTime);
		this.cTag = cTag;
		this.deleted = deleted;
		this.description = description;
		this.eTag = eTag;
		this.file = file;
		this.fileSystemInfo = fileSystemInfo;
		this.image = image;
		this.lastModifiedBy = lastModifiedBy;
		this.lastModifiedDateTime = BaseContainer.parseDateTime(lastModifiedDateTime);
		this.location = location;
		this.name = name;
		this.parentReference = parentReference;
		this.photo = photo;
		this.remoteItem = remoteItem;
		this.searchResult = searchResult;
		this.shared = shared;
		this.sharePointIds = sharePointIds;
		this.size = size;
		this.video = video;
		this.webDavUrl = webDavUrl;
		this.webUrl = webUrl;
	}

	public String getCRC32() {
		return this.file.getCrc32Hash();
	}

	public String getSHA1() {
		return this.file.getSha1Hash();
	}

	public String getQuickXorHash() {
		return this.file.getQuickXorHash();
	}

	/**
	 * @see FileItem#download(Path)
	 */
	@NotNull
	public void download(@NotNull String path) throws IOException, FileDownFailException {
		this.download(Paths.get(path));
	}

	/**
	 * Download file from OneDrive to {@code path}.<br>
	 * It could be relative path (like . or ..).<br>
	 * If {@code path} is just directory path, automatically naming as {@code getName()}.<br>
	 * <br>
	 * If {@code path} is file path and already exists, it will throw {@link FileAlreadyExistsException}.
	 *
	 * @param path File or folder path. It could be either parent folder(without filename) for download
	 *             or specific file path (with filename).
	 * @throws IOException                If an I/O error occurs, which is possible because the construction of the
	 *                                    canonical pathname may require filesystem queries
	 * @throws SecurityException          If a required system property value cannot be accessed, or if a security
	 *                                    manager exists and its SecurityManager.checkRead method denies read access to
	 *                                    the file
	 * @throws FileAlreadyExistsException If {@code path} is file path and already exists.
	 * @throws FileDownFailException      If fail to download with not 200 OK response.
	 */
	@NotNull
	public void download(@NotNull Path path) throws IOException, FileDownFailException {
		path = path.toAbsolutePath();
		Files.createDirectories(path.getParent());

		HttpsResponse response = OneDriveRequest.doGet(
				"/drive/items/" + id + "/content",
				client.getAccessToken());

		if (response.getCode() != 200) {
			throw new FileDownFailException(
					String.format("File download fails with %d %s", response.getCode(), response.getMessage()));
		}

		AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(path,
				StandardOpenOption.CREATE,
				StandardOpenOption.WRITE);

		ByteBuffer contentBuf = ByteBuffer.wrap(response.getContent());

		fileChannel.write(contentBuf, 0);
	}
}