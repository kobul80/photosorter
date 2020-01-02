package cz.kobul.photosorter;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.mp4.Mp4Directory;

public class PhotoSorter {

    public static Date extractDate(File file) {
        Metadata metadata = null;
        try {
            metadata = ImageMetadataReader.readMetadata(file);
        } catch (Exception ex) {
            System.err.println("Unable to read metadata of file " + file.getName());
        }

        if (metadata != null) {
            ExifIFD0Directory exifDirectory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
            if (exifDirectory != null) {
                Date date = exifDirectory.getDate(ExifIFD0Directory.TAG_DATETIME, TimeZone.getDefault());
                if (date != null) {
                    return date;
                }
            }

            ExifSubIFDDirectory exif2Directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            if (exif2Directory != null) {
                Date date = exif2Directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_DIGITIZED, TimeZone.getDefault());
                if (date != null) {
                    return date;
                }
            }

            Mp4Directory mp4Directory = metadata.getFirstDirectoryOfType(Mp4Directory.class);
            if (mp4Directory != null) {
                Date date = mp4Directory.getDate(Mp4Directory.TAG_CREATION_TIME, TimeZone.getDefault());
                if (date != null) {
                    return date;
                }
            }
//            System.out.println(metadata.getDirectories());
        }

        return new Date(file.lastModified());
    }


    public static void main(String[] args) {
        System.out.println("Program tridi fotky do slozek podle datumu ve formatu 'YYYY_MM_DD__'");
        if (args.length != 1) {
            System.out.println("Parametr: Jmeno adresare");
            return;
        }
        String dir = args[0];
        final File[] files = new File(dir).listFiles();

        SimpleDateFormat format = new SimpleDateFormat("yyyy_MM_dd" + "__");
        
        for (File file : files) {
            if (file.isFile()) {
                System.out.print("Processing " + file.getName() + " ... ");
                Date date = extractDate(file);
                System.out.println("detected date " + date);
                File destination = new File(dir + '/' + format.format(date));
                destination.mkdirs();
                System.out.print("Moving " + file.getName() + " to " + destination.getAbsolutePath() + " ... ");
                try {
                    file.renameTo(new File(destination.getAbsolutePath() + '/' + file.getName()));
                    System.out.println("done.");
                } catch (Exception ex) {
                    System.out.println("error [" + ex.getMessage() + "]");
                }
            }
        }
    }

}
