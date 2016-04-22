package com.example.johnpconsidine.blemap;

import android.bluetooth.le.ScanRecord;
import android.os.ParcelUuid;

/**
 * Created by Jerry on 04/22/2016.
 */
public class In_Marker {
    private String user_id;
    private byte event_type;
    private float longitude;
    private float latitude;
    private String notes;

    public static final ParcelUuid BASIC_SERVICE = ParcelUuid.fromString("00001809-0000-1000-8000-00805f9b34fb");
    public static final ParcelUuid NOTE_SERVICE = ParcelUuid.fromString("00001809-0000-1000-8000-85e4431e713a");

    public In_Marker (ScanRecord basic_record){

        byte[] user_id_byte = new byte[14];
        byte[] basic_data = basic_record.getServiceData(BASIC_SERVICE);

        if (basic_data != null){
            latitude = parseData(basic_data, "Lat");
            longitude = parseData(basic_data, "Long");
            event_type = basic_data [8];
            System.arraycopy(basic_data, 9, user_id_byte, 0, 14);
            user_id = new String(user_id_byte);
            //notes = new String(basic_data);
        }
    }


    public float parseData(byte[] serviceData, String flag){
        float out = 0f;

        if (flag.equals("Lat")){
            out = ArrayToFloat(serviceData, 0);
        }else if(flag.equals("Long")){
            out = ArrayToFloat(serviceData, 4);
        }
        return out;
    }

    //transform byte[] array to float
    public float ArrayToFloat(byte[] Array,int Pos)
    {
        int accum = 0;
        accum = Array[Pos+0] & 0xFF;
        accum |= (long)(Array[Pos+1] & 0xFF)<<8;
        accum |= (long)(Array[Pos+2] & 0xFF)<<16;
        accum |= (long)(Array[Pos+3] & 0xFF)<<24;
        return Float.intBitsToFloat(accum);
    }

    public String getUser_id(){
        return user_id;
    }

    public byte getEvent_type(){
        return event_type;
    }

    public float getLongitude(){
        return longitude;
    }

    public float getLatitude(){
        return latitude;
    }

    public String getNotes(){
        return notes;
    }


/*    public void setUser_id(String user_id){
        this.user_id = user_id;
    }

    public void setEvent_type(byte event_type){
        this.event_type = event_type;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }*/
}
