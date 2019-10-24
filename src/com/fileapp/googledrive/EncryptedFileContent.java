package com.fileapp.googledrive;

import com.fileapp.utils.Crypto;
import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.util.Preconditions;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Clone of FileContent with encryption
 */
public class EncryptedFileContent extends AbstractInputStreamContent {
    private final File file;
    private final String key;

    public
    EncryptedFileContent(String type, File file, String key) {
        super(type);
        this.file = (File) Preconditions.checkNotNull(file);
        this.key = key;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return Crypto.getEncryptedInputStream(this.file, this.key);
    }

    @Override
    public long getLength() throws IOException {
        long fileLength = this.file.length();
        long extraLength = 16 - (fileLength%16);
        return fileLength + extraLength;
    }

    @Override
    public boolean retrySupported() {
        return false;
    }
}
