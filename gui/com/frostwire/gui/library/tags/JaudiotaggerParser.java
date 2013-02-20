package com.frostwire.gui.library.tags;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.audio.generic.AudioFileReader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.images.Artwork;

import com.frostwire.jpeg.JPEGImageIO;

class JaudiotaggerParser extends AbstractTagParser {

    private static final Log LOG = LogFactory.getLog(JaudiotaggerParser.class);

    private final AudioFileReader fileReader;

    public JaudiotaggerParser(File file, AudioFileReader fileReader) {
        super(file);
        this.fileReader = fileReader;
    }

    public JaudiotaggerParser(File file) {
        this(file, null);
    }

    @Override
    public TagsData parse() {
        TagsData data = null;

        try {
            AudioFile audioFile = fileReader != null ? fileReader.read(file) : AudioFileIO.read(file);

            AudioHeader header = audioFile.getAudioHeader();

            int duration = header.getTrackLength();
            String bitrate = header.getBitRate();

            String title = getTitle(audioFile);
            String artist = getArtist(audioFile);
            String album = getAlbum(audioFile);
            String comment = getComment(audioFile);
            String genre = getGenre(audioFile);
            String track = getTrack(audioFile);
            String year = getYear(audioFile);

            data = sanitize(duration, bitrate, title, artist, album, comment, genre, track, year);

        } catch (Exception e) {
            LOG.warn("Unable to parse file using Jaudiotagger: " + file, e);
        }

        return data;
    }

    @Override
    public BufferedImage getArtwork() {
        BufferedImage data = null;

        try {
            AudioFile audioFile = fileReader != null ? fileReader.read(file) : AudioFileIO.read(file);

            Artwork artwork = audioFile.getTag().getFirstArtwork();

            byte[] imageData = artwork.getBinaryData();

            data = imageFromData(imageData);

        } catch (Exception e) {
            LOG.warn("Unable to read artwork of file using Jaudiotagger: " + file, e);
        }

        return data;
    }

    protected String getTitle(AudioFile audioFile) {
        return getValueSafe(audioFile.getTag(), FieldKey.TITLE);
    }

    protected String getArtist(AudioFile audioFile) {
        return getValueSafe(audioFile.getTag(), FieldKey.ARTIST);
    }

    protected String getAlbum(AudioFile audioFile) {
        return getValueSafe(audioFile.getTag(), FieldKey.ALBUM);
    }

    protected String getComment(AudioFile audioFile) {
        return getValueSafe(audioFile.getTag(), FieldKey.COMMENT);
    }

    protected String getGenre(AudioFile audioFile) {
        return getValueSafe(audioFile.getTag(), FieldKey.GENRE);
    }

    protected String getTrack(AudioFile audioFile) {
        return getValueSafe(audioFile.getTag(), FieldKey.TRACK);
    }

    protected String getYear(AudioFile audioFile) {
        return getValueSafe(audioFile.getTag(), FieldKey.YEAR);
    }

    protected BufferedImage imageFromData(byte[] data) {
        BufferedImage image = null;
        try {
            try {
                image = ImageIO.read(new ByteArrayInputStream(data, 0, data.length));
            } catch (IIOException e) {
                image = JPEGImageIO.read(new ByteArrayInputStream(data, 0, data.length));
            }
        } catch (Throwable e) {
            LOG.error("Unable to create artwork image from bytes", e);
        }

        return image;
    }

    private String getValueSafe(Tag tag, FieldKey id) {
        String value = null;

        try {
            value = tag.getFirst(id);
        } catch (Exception e) {
            LOG.warn("Unable to get value for key: " + id, e);
        }

        return value;
    }
}
