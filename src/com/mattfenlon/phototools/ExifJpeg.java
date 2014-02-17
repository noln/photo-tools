package com.mattfenlon.phototools;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import com.jammyapps.utils.ByteTools;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;

public class ExifJpeg {
	/**
	 * ExifJpeg
	 * 
	 * A hastily hacked class to encapsulate Exif Jpeg operations on an object.
	 * First drop, exceptions are hacked.
	 * 
	 * NOT PRODUCTION CODE!
	 * 
	 * @author Matt Fenlon
	 */
	
	private boolean debug = true;
	
	private Context mContext;
	private Uri mUri;
	
	private String filename = "NoName";
	private Bitmap thumbnail = null;
	private int iso = 0;
	
	private String exposure_time;
	private Double exposure_time_value;
	
	private Double aperture;
	private String focal_length;
	private String LOG_TAG = "ExifJpeg";
	private String datetime;
	
	private String GpsAltitude, GpsAltitudeRef;
	private String GpsLongitude, GpsLongitudeRef;
	private String GpsLatitude, GpsLatitudeRef;
	private String GpsProcessingMethod;
	private String GpsTimestamp;
	
	private String ImageLength, ImageWidth;
	
	private int flash;
	
	private String make, model;
	
	//private orientation mOrientation;
	private String mOrientation;
	private int mOrientationVal;
	private String WhiteBalance;
	
	private class orientation{	// TODO - Tidy up!
		public int flip_horizontal,
			flip_vertical,
			normal,
			rotate_180,
			rotate_270,
			rotate_90,
			transport,
			transverse,
			undefined;
	}
	
	public ExifJpeg(){
		// Take no action.
	}
	
	public ExifJpeg(String filePath){
		// Pun to another constructor with a null display value.
		// The other constructor will handle the null pointer issue.
		this(filePath,null);
	}
	
	public ExifJpeg(Uri u, Context c) throws IOException{
		
		this.mUri = u;
		this.mContext = c;
		
		// Find out where to output temp files.
		File outputDir = mContext.getCacheDir(); // context being the Activity pointer
		
		// Create the temp file to use.
		File oFile = File.createTempFile("gdrive_photo", "jpeg", outputDir);
		
		// Open an inputstream from the source provided to the constructor.
		InputStream iStream = mContext.getContentResolver().openInputStream(u);
		
		/**	Create a byte array to hold the content that exists at the 
	 	 *	Uri we're interested in; this preserves all of the data that
	 	 *	exists within the file, including any JPEG meta data. If 
	 	 *	you punt this straight to a Bitmap object, you'll lose all 
	 	 *	of that.
	 	 */
	 	byte[] inputData = ByteTools.getBytes(iStream);
	 	
	 	if (debug) Log.d(LOG_TAG, outputDir.getPath());
	 	ByteTools.writeFile(inputData,oFile.getPath());
	 	
	 	// Run the usual init() function, using the file path of the file
	 	// we just made.
	 	init(oFile.getPath(), null);
	 	
	 	if (debug && (this.mUri != null)){
			Log.d(LOG_TAG,"Uri Real Path: " + getRealPathFromURI(mUri));
		} else {
			Log.d(LOG_TAG,"Uri Real Path: Uri is null");
		}
		
	}
	
	private void init(String filePath, Display display) {
		if (debug) Log.d(LOG_TAG,"*** ExifJpeg created ***");
		
		try {
			// Path
			ExifInterface exif = new ExifInterface(filePath);
			if (debug) Log.d(LOG_TAG,"> Path: " + filePath);
			
			// Filename
			String[] path_arr = filePath.split("\\/");
			this.filename = path_arr[path_arr.length-1];
			if (debug) Log.d(LOG_TAG,"> Filename: " + this.filename);
			
			// ISO
			if(getAttributeAsString(ExifInterface.TAG_ISO, exif) != null){
				this.iso = Integer
							.parseInt(
									getAttributeAsString(
											ExifInterface.TAG_ISO, exif));
				
				if (debug) Log.d(LOG_TAG,"> ISO: "+Integer.toString(this.iso));
			} else {
				this.iso = 0;
				if (debug) Log.d(LOG_TAG,"> ISO: " 
												+ Integer.toString(this.iso) 
												+ ", N/A");
			}
			
			// Exposure Time
			if(getAttributeAsString(ExifInterface.TAG_EXPOSURE_TIME, exif) != null){
				//this.shutter_speed = Double.parseDouble(getAttributeAsString(ExifInterface.TAG_EXPOSURE_TIME, exif));
				String sinkhole = getExposureTime(); // TODO - Tidy up!
				this.exposure_time = exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME);
			} else {
				this.exposure_time = "not available";
			}
			
			// Aperture
			if(getAttributeAsString(ExifInterface.TAG_APERTURE, exif) != null){
				this.aperture = Double.parseDouble(getAttributeAsString(ExifInterface.TAG_APERTURE, exif));
			} else {
				this.aperture = null;
			}
			
			// Flash
			if(getAttributeAsString(ExifInterface.TAG_FLASH, exif) != null){
				this.flash = Integer.parseInt(getAttributeAsString(ExifInterface.TAG_FLASH, exif));
			} else {
				this.flash = 1337;
			}
			
			// Focal Length
			if(getAttributeAsString(ExifInterface.TAG_FOCAL_LENGTH, exif) != null){
				this.focal_length = getAttributeAsString(ExifInterface.TAG_FOCAL_LENGTH, exif);
			} else {
				this.focal_length = null;
			}
			
			// GPS Altitude
			if(getAttributeAsString(ExifInterface.TAG_GPS_ALTITUDE, exif) != null){
				this.GpsAltitude = getAttributeAsString(ExifInterface.TAG_GPS_ALTITUDE, exif);
			} else {
				this.GpsAltitude = null;
			}
			
			// GPS Altitude Ref.
			if(getAttributeAsString(ExifInterface.TAG_GPS_ALTITUDE_REF, exif) != null){
				this.GpsAltitudeRef = getAttributeAsString(ExifInterface.TAG_GPS_ALTITUDE_REF, exif);
			} else {
				this.GpsAltitudeRef = null;
			}
			
			// GPS Latitude
			if(getAttributeAsString(ExifInterface.TAG_GPS_LATITUDE, exif) != null){
				this.GpsLatitude = getAttributeAsString(ExifInterface.TAG_GPS_LATITUDE, exif);
			} else {
				this.GpsLatitude = null;
			}
			
			// GPS Latitude Ref.
			if(getAttributeAsString(ExifInterface.TAG_GPS_LATITUDE_REF, exif) != null){
				this.GpsLatitudeRef = getAttributeAsString(ExifInterface.TAG_GPS_LATITUDE_REF, exif);
			} else {
				this.GpsLatitudeRef = null;
			}
			
			// GPS Longitude
			if(getAttributeAsString(ExifInterface.TAG_GPS_LONGITUDE, exif) != null){
				this.GpsLongitude = getAttributeAsString(ExifInterface.TAG_GPS_LONGITUDE, exif);
			} else {
				this.GpsLongitude = null;
			}
			
			// GPS Longitude Ref.
			if(getAttributeAsString(ExifInterface.TAG_GPS_LONGITUDE_REF, exif) != null){
				this.GpsLongitudeRef = getAttributeAsString(ExifInterface.TAG_GPS_LONGITUDE_REF, exif);
			} else {
				this.GpsLongitudeRef = null;
			}
			
			// GPS Processing Method
			if(getAttributeAsString(ExifInterface.TAG_GPS_PROCESSING_METHOD, exif) != null){
				this.GpsProcessingMethod = getAttributeAsString(ExifInterface.TAG_GPS_PROCESSING_METHOD, exif);
			} else {
				this.GpsProcessingMethod = null;
			}
			
			// GPS Timestamp
			if(getAttributeAsString(ExifInterface.TAG_GPS_TIMESTAMP, exif) != null){
				this.GpsTimestamp = getAttributeAsString(ExifInterface.TAG_GPS_TIMESTAMP, exif);
			} else {
				this.GpsTimestamp = null;
			}
			
			// Image Length
			if(getAttributeAsString(ExifInterface.TAG_IMAGE_LENGTH, exif) != null){
				this.ImageLength = getAttributeAsString(ExifInterface.TAG_IMAGE_LENGTH, exif);
			} else {
				this.ImageLength = null;
			}
			
			// Image Width
			if(getAttributeAsString(ExifInterface.TAG_IMAGE_WIDTH, exif) != null){
				this.ImageWidth = getAttributeAsString(ExifInterface.TAG_IMAGE_WIDTH, exif);
			} else {
				this.ImageWidth = null;
			}
			
			// Make
			if(getAttributeAsString(ExifInterface.TAG_MAKE, exif) != null){
				this.make = getAttributeAsString(ExifInterface.TAG_MAKE, exif);
			} else {
				this.make = null;
			}
			
			// Model
			if(getAttributeAsString(ExifInterface.TAG_MODEL, exif) != null){
				this.model = getAttributeAsString(ExifInterface.TAG_MODEL, exif);
			} else {
				this.model = null;
			}
			
			// Orientation
			if(getAttributeAsString(ExifInterface.TAG_ORIENTATION, exif) != null){
				this.mOrientation = getAttributeAsString(ExifInterface.TAG_ORIENTATION, exif);
				this.mOrientationVal = Integer.parseInt(this.mOrientation);
			} else {
				this.mOrientation = null;
				this.mOrientationVal = 0;
			}
			
			// White Balance
			if(getAttributeAsString(ExifInterface.TAG_WHITE_BALANCE, exif) != null){
				this.WhiteBalance = getAttributeAsString(ExifInterface.TAG_WHITE_BALANCE, exif);
			} else {
				this.WhiteBalance = null;
			}

			if (display != null){
			
				Point size = new Point();
		    	display.getSize(size);
		    	int screen_width = size.x;
		    	int screen_height = size.y;	// TODO - Required?
				
				File imgFile = new  File(filePath);
				if(imgFile.exists()){
				    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
				    int nh = (int) ( myBitmap.getHeight() * (Double.valueOf(screen_width) / myBitmap.getWidth()) );
		        	this.thumbnail = Bitmap.createScaledBitmap(myBitmap, screen_width, nh, true);
				}
			
			}
			
		} catch (IOException e) {
			Log.e(LOG_TAG,"Couldn't get Exif data from image.");
			e.printStackTrace();
		}
	}

	public ExifJpeg(String filePath, Display display){
		init(filePath, display);
	}
	
	private String getAttributeAsString(String tag, ExifInterface exif)
    {
    	return(exif.getAttribute(tag));
    }
	
	public String getExposureTime(){
		String strSSpd = "eRR";
		Double processed_shutter_speed = 0.0;
		
		if (debug) Log.d(LOG_TAG,"** getExposureTime() for " + this.filename);
		
		try{
			exposure_time_value = Double.parseDouble(this.exposure_time);
		} catch (Exception e){
			exposure_time_value = 1.0;
		}
		
		if (exposure_time_value <= 1){
			if (debug) Log.d(LOG_TAG, this.exposure_time + " is less than 0.");
			processed_shutter_speed = 1/exposure_time_value;
			int i = (int) Math.round(processed_shutter_speed);
			strSSpd = "1/" + Integer.toString(i);
		} else {
			if (debug) Log.d(LOG_TAG, this.exposure_time + " is GREATER than 0.");
			strSSpd = Long.toString(Math.round(exposure_time_value)) + "\"";
		}
		
		if (debug) Log.d(LOG_TAG,"ShutterSpeed (before): " + this.exposure_time);
		if (debug) Log.d(LOG_TAG,"ShutterSpeed (proc.) : " + strSSpd);
		
		return strSSpd;
	}
	
	public String getAperture() {
		
		String strAperture;
		
		int no_decimal = 0;
		if (aperture%1 == 0){
			no_decimal = aperture.intValue();
			strAperture = Integer.toString(no_decimal);
		} else {
			strAperture = Double.toString(aperture);
		}
		
		if (debug) Log.d(LOG_TAG,"F-Stop: " + strAperture);
		
		return "F" + strAperture;
	}
	
	public void setFilename(String filename){
		this.filename = filename;
	}
	
	public String getFilename(){
		return this.filename;
	}
	
	public void setThumbnail(){
		// TODO
	}
	
	public Bitmap getThumbnail(){
		// TODO
		return this.thumbnail;
	}
	
	public void setISO(int iso){
		this.iso = iso;
	}
	
	public int getISO(){
		if (debug) Log.d(LOG_TAG , "ISO set to: " + Integer.toString(this.iso));
		return this.iso;
	}
	
	private String getStrAperture(){
		String aperture_value;
		try{
			aperture_value = "F" + Double.toString(this.aperture);
		} catch (Exception e){
			aperture_value = null;
		}
		return "Aperture: " + aperture_value;
	}
	
	private String getStrDateTime(){
		String datetime_value = this.datetime;
		return "Taken: " + datetime_value;
	}
	
	private String getStrExposureTime(){
		String denominator = "0";
		String exposuretime_str = null;
		
		try{
			denominator = Integer.toString((int)Math.round(1/Double.parseDouble(this.exposure_time)));
			exposuretime_str = "1/" + denominator + " (" + this.exposure_time + " seconds)";
		} catch (Exception e){
			exposuretime_str = null;
		}
		
		return "Exposure Time: " + exposuretime_str;
	}
	
	private String getFlashDescription(int i) {
		
		String strFlash = null;
		
		switch(i){	// TODO - Put this in an XML resource, like orientation.
			case 0: strFlash = "Flash did not fire";
				break;
			case 1: strFlash = "Flash fired";
				break;
			case 5: strFlash = "Strobe return light not detected";
				break;
			case 7: strFlash = "Strobe return light detected";
				break;
			case 9: strFlash = "Flash fired, compulsory flash mode";
				break;
			case 13: strFlash = "Flash fired, compulsory flash mode, return light not detected";
				break;
			case 15: strFlash = "Flash fired, compulsory flash mode, return light detected";
				break;
			case 16: strFlash = "Flash did not fire, compulsory flash mode";
				break;
			case 24: strFlash = "Flash did not fire, auto mode";
				break;
			case 25: strFlash = "Flash fired, auto mode";	
				break;
			case 29: strFlash = "Flash fired, auto mode, return light not detected";	
				break;
			case 31: strFlash = "Flash fired, auto mode, return light detected";	
				break;
			case 32: strFlash = "No flash function";	
				break;
			case 65: strFlash = "Flash fired, red-eye reduction mode";	
				break;
			case 69: strFlash = "Flash fired, red-eye reduction mode, return light not detected";	
				break;
			case 71: strFlash = "Flash fired, red-eye reduction mode, return light detected";	
				break;
			case 73: strFlash = "Flash fired, compulsory flash mode, red-eye reduction mode";	
				break;
			case 77: strFlash = "Flash fired, compulsory flash mode, red-eye reduction mode, return light not detected";	
				break;
			case 79: strFlash = "Flash fired, compulsory flash mode, red-eye reduction mode, return light detected";	
				break;
			case 89: strFlash = "Flash fired, auto mode, red-eye reduction mode";	
				break;
			case 93: strFlash = "Flash fired, auto mode, return light not detected, red-eye reduction mode";	
				break;
			case 95: strFlash = "Flash fired, auto mode, return light detected, red-eye reduction mode";	
				break;
			default: strFlash = null;
            	break;
		}
		
		return strFlash;
	}
	
	private String getStrFlash() {
		return "Flash: " + getFlashDescription(this.flash);
	}
	
	private String getStrFocalLength() {
		String[] fl_arr = this.focal_length.split("/");
		String focal_length_str = Integer.toString(Integer.parseInt(fl_arr[0])/Integer.parseInt(fl_arr[1]));
		focal_length_str = focal_length_str + "mm";
		return "Focal Length: " + focal_length_str;
	}
	
	private String getStrGpsAltitude() {
		
		return "GPS Altitude: " + this.GpsAltitude;
	}
	
	private String getStrGpsAltitudeRef() {
		
		return "GPS Altitude Ref.: " + this.GpsAltitudeRef;
	}
	
	private String getStrGpsLongitude() {
		
		return "GPS Longitude: " + this.GpsLongitude;
	}
	
	private String getStrGpsLongitudeRef() {
		
		return "GPS Longitude Ref.: " + this.GpsLongitudeRef;
	}
	
	private String getStrGpsLatitude() {
		
		return "GPS Latitude: " + this.GpsLatitude;
	}
	
	private String getStrGpsLatitudeRef() {
		
		return "GPS Latitude Ref.: " + this.GpsLatitudeRef;
	}
	
	private String getStrGpsProcessingMethod() {
		
		return "GPS Processing Method: " + this.GpsProcessingMethod;
	}
	
	private String getStrGpsTimestamp() {
		
		return "GPS Timestamp: " + this.GpsTimestamp;
	}
	
	private String getStrImageLength() {
		
		return "Image Length (px): " + this.ImageLength;
	}
	
	private String getStrImageWidth() {
		
		return "Image Width (px): " + this.ImageWidth;
	}
	
	private String getStrISO() {
		String iso_str = Integer.toString(this.iso);
		return "ISO: " + iso_str;
	}
	
	private String getStrMake() {

		return "Make: " + this.make;
	}
	
	private String getStrModel() {

		return "Model: " + this.model;
	}
	
	private String getStrOrientation() {
		String orientation_str;
		String[] oVals = mContext.getResources().getStringArray(R.array.orientation_values);
		orientation_str = oVals[this.mOrientationVal];
		return "Orientation: " + orientation_str;
	}
	
	private String getStrWhiteBalance() {
		String white_balance_str;
		if (Integer.parseInt(this.WhiteBalance) == 0){
			white_balance_str = "Auto";
		} else {
			white_balance_str = "Manual";
		}
		return "White Balance: " + white_balance_str;
	}
	
	private String getRealPathFromURI(Uri contentUri) {
	    
		// TODO - Rewrite for robustness. Currently useless.
		
		if (debug) Log.d(LOG_TAG, "getRealPathFromURI(" + contentUri.toString());
		
		String[] proj = { MediaStore.Images.Media.DATA };
	    CursorLoader loader = new CursorLoader(mContext, contentUri, proj, null, null, null);
	    Cursor cursor = loader.loadInBackground();
	    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
	    if (debug) Log.d(LOG_TAG, "getRealPathFromURI: " + Integer.toString(column_index));
	    cursor.moveToFirst();
	    return cursor.getString(column_index);
	}
	
	public String toString(){
		String strExifJpeg = "*** ExifJpeg.toString() ***\n";
		strExifJpeg = strExifJpeg + "Filename: " + this.filename + "\n";
		//strExifJpeg = "*  " + strExifJpeg + "Realpath: " + getRealPathFromURI(this.mUri) + "\n";
		strExifJpeg = strExifJpeg + getStrAperture() + "\n";	// TAG_APERTURE
		strExifJpeg = strExifJpeg + getStrDateTime() + "\n";	// TAG_DATETIME
		strExifJpeg = strExifJpeg + getStrExposureTime() + "\n";// TAG_EXPOSURE_TIME
		strExifJpeg = strExifJpeg + getStrFlash() + "\n";// TAG_FLASH
		strExifJpeg = strExifJpeg + getStrFocalLength() + "\n";// TAG_FOCAL_LENGTH
		strExifJpeg = strExifJpeg + getStrGpsAltitude() + "\n";// TAG_GPS_ALTITUDE
		strExifJpeg = strExifJpeg + getStrGpsAltitudeRef() + "\n";// TAG_GPS_ALTITUDE_REF
		strExifJpeg = strExifJpeg + getStrGpsLongitude() + "\n";// TAG_GPS_LONGITUDE
		strExifJpeg = strExifJpeg + getStrGpsLongitudeRef() + "\n";// TAG_GPS_LONGITUDE_REF
		strExifJpeg = strExifJpeg + getStrGpsLatitude() + "\n";// TAG_GPS_LATITUDE
		strExifJpeg = strExifJpeg + getStrGpsLatitudeRef() + "\n";// TAG_GPS_LATITUDE_REF
		strExifJpeg = strExifJpeg + getStrGpsProcessingMethod() + "\n";// TAG_GPS_PROCESSING_METHOD
		strExifJpeg = strExifJpeg + getStrGpsTimestamp() + "\n";// TAG_GPS_TIMESTAMP
		strExifJpeg = strExifJpeg + getStrImageLength() + "\n";// TAG_IMAGE_LENGTH
		strExifJpeg = strExifJpeg + getStrImageWidth() + "\n";// TAG_IMAGE_WIDTH
		strExifJpeg = strExifJpeg + getStrISO() + "\n";// TAG_ISO
		strExifJpeg = strExifJpeg + getStrMake() + "\n";// TAG_MAKE
		strExifJpeg = strExifJpeg + getStrModel() + "\n";// TAG_MODEL
		strExifJpeg = strExifJpeg + getStrOrientation() + "\n";// TAG_ORIENTATION
		strExifJpeg = strExifJpeg + getStrWhiteBalance() + "\n";// TAG_WHITE_BALANCE
		return strExifJpeg;
	}

}
