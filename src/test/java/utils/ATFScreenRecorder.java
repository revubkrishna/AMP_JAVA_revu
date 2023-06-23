package utils;

import static org.monte.media.FormatKeys.EncodingKey;
import static org.monte.media.FormatKeys.FrameRateKey;
import static org.monte.media.FormatKeys.KeyFrameIntervalKey;
import static org.monte.media.FormatKeys.MIME_AVI;
import static org.monte.media.FormatKeys.MediaTypeKey;
import static org.monte.media.FormatKeys.MimeTypeKey;
import static org.monte.media.VideoFormatKeys.CompressorNameKey;
import static org.monte.media.VideoFormatKeys.DepthKey;
import static org.monte.media.VideoFormatKeys.ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE;
import static org.monte.media.VideoFormatKeys.QualityKey;

import java.awt.AWTException;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;

import org.monte.media.Format;
import org.monte.media.FormatKeys.MediaType;
import org.monte.media.Registry;
import org.monte.media.math.Rational;
import org.monte.screenrecorder.ScreenRecorder;

public class ATFScreenRecorder extends ScreenRecorder
{
   private static ScreenRecorder screenRecorder;
   private final String name;
   private static String videoPath;
   public static String fileSavedFormat = "";


   public ATFScreenRecorder(GraphicsConfiguration cfg, Rectangle captureArea, Format fileFormat, Format screenFormat, Format mouseFormat, Format audioFormat, File movieFolder, String fileName)
      throws IOException,
      AWTException
   {
      super(cfg, captureArea, fileFormat, screenFormat, mouseFormat, audioFormat, movieFolder);
      name = fileName;
   }


   @Override
   protected File createMovieFile(Format fileFormat)
      throws IOException
   {
      File f = new File(videoPath);
      if ( !f.exists())
      {
         f.mkdirs();
      }
      fileSavedFormat = "." + Registry.getInstance().getExtension(fileFormat);
      return new File(f, name + "." + Registry.getInstance().getExtension(fileFormat));
   }


   public static void StartRecording(String filePath, String methodName)
   {
      videoPath = filePath;
      File file = new File(filePath);
      GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
      try
      {
         screenRecorder = new ATFScreenRecorder(gc, gc.getBounds(), new Format(MediaTypeKey, MediaType.FILE, MimeTypeKey, MIME_AVI), new Format(MediaTypeKey, MediaType.VIDEO, EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE, CompressorNameKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE, DepthKey, 24, FrameRateKey, Rational.valueOf(15), QualityKey, 1.0f, KeyFrameIntervalKey, 15 * 60), new Format(MediaTypeKey, MediaType.VIDEO, EncodingKey, "black", FrameRateKey, Rational.valueOf(30)), null, file, methodName);
         screenRecorder.start();
      } catch (Exception e)
      {
         e.printStackTrace();
      }
   }


   public static void StopRecording()
   {
      try
      {
         screenRecorder.stop();
      } catch (IOException e)
      {
         e.printStackTrace();
      }
   }
}