package com.vonchenchen.mybledemo.utils;

import android.location.Address;

/**
 * Created by vonchenchen on 2016/2/25 0025.
 */
public class BLEUtils {

    //BLE System service
    public final static String BLE_UUID_ALERT_NOTIFICATION_SERVICE                = "1811";     /**< Alert Notification service UUID. */
    public final static String BLE_UUID_BATTERY_SERVICE                             = "180f";     /**< Battery service UUID. */
    public final static String BLE_UUID_BLOOD_PRESSURE_SERVICE                          ="1810";     /**< Blood Pressure service UUID. */
    public final static String BLE_UUID_CURRENT_TIME_SERVICE                            ="1805";    /**< Current Time service UUID. */
    public final static String BLE_UUID_CYCLING_SPEED_AND_CADENCE                       ="1816";     /**< Cycling Speed and Cadence service UUID. */
    public final static String BLE_UUID_DEVICE_INFORMATION_SERVICE                      ="180a";     /**< Device Information service UUID. */
    public final static String BLE_UUID_GLUCOSE_SERVICE                                 ="1808";     /**< Glucose service UUID. */
    public final static String BLE_UUID_HEALTH_THERMOMETER_SERVICE                     = "1809";     /**< Health Thermometer service UUID. */
    public final static String BLE_UUID_HEART_RATE_SERVICE                             = "180d";     /**< Heart Rate service UUID. */
    public final static String BLE_UUID_HUMAN_INTERFACE_DEVICE_SERVICE                  ="1812";     /**< Human Interface Device service UUID. */
    public final static String BLE_UUID_IMMEDIATE_ALERT_SERVICE                         ="1802";     /**< Immediate Alert service UUID. */
    public final static String BLE_UUID_LINK_LOSS_SERVICE                              = "1803";     /**< Link Loss service UUID. */
    public final static String BLE_UUID_NEXT_DST_CHANGE_SERVICE                        = "1807";     /**< Next Dst Change service UUID. */
    public final static String BLE_UUID_PHONE_ALERT_STATUS_SERVICE                     = "180e";     /**< Phone Alert Status service UUID. */
    public final static String BLE_UUID_REFERENCE_TIME_UPDATE_SERVICE                 =  "1806";     /**< Reference Time Update service UUID. */
    public final static String BLE_UUID_RUNNING_SPEED_AND_CADENCE                      = "1814";     /**< Running Speed and Cadence service UUID. */
    public final static String BLE_UUID_SCAN_PARAMETERS_SERVICE                        = "1813";     /**< Scan Parameters service UUID. */
    public final static String BLE_UUID_TX_POWER_SERVICE                               = "1804";     /**< TX Power service UUID. */

    //GATT properties Type
    public final static String BLE_UUID_PRIMARY_SERVICE_PROPERTY = "2800";
    public final static String BLE_UUID_SECONDARY_SERVICE_PROPERTY = "2801";
    public final static String BLE_UUID_INCLUDE_PROPERTY = "2802";
    public final static String BLE_UUID_CHARACTERISTIC_PROPERTY = "2803";

    //GATT Charactorstic Type
    public final static String BLE_UUID_DEVICE_NAME_CHARACTERISTIC = "2a00";
    public final static String BLE_UUID_APPEARACE_CHARACTERISTIC = "2a01";
    public final static String BLE_UUID_PERIPHERAL_PRIVACY_FLAG_CHARACTERISTIC = "2a02";
    public final static String BLE_UUID_RECONNECTION_ADDRESS_CHARACTERISTIC = "2a03";
    public final static String BLE_UUID_PERIPHERAL_PREFERRED_CONNECTION_PARAMETERS_CHARACTERISTIC = "2a04";
    public final static String BLE_UUID_SERVICE_CHANGED_CHARACTERISTIC = "2a05";

    //GATT Charactorstic descripter
    public final static String BLE_UUID_CHARACTERISTIC_EXTENDED_PROPERTIES_CHARACTERISTIC = "2900";
    public final static String BLE_UUID_CHARACTERISTIC_USER_DESCRIPTION_CHARACTERISTIC = "2901";
    public final static String BLE_UUID_CHARACTERISTIC_CLIENT_CONFIGURATION_CHARACTERISTIC = "2902";
    public final static String BLE_UUID_CHARACTRRISTIC_SERVER_CONFIGURATION_CHARACTERISTIC = "2903";
    public final static String BLE_UUID_CHARACTERISTIC_FORMAT_CHARACTERISTIC = "2904";
    public final static String BLE_UUID_CHARACTERISTIC_AGGREGATE_FORMAT_CHARACTERISTIC = "2905";

    public static String getBLEServiceInfo(String uuidHead){

        if("1800".equals(uuidHead)){
            return "Generic Access Profile";
        }else if("1801".equals(uuidHead)){
            return "Generic Attribute Profile";
        }else if(BLE_UUID_ALERT_NOTIFICATION_SERVICE.equals(uuidHead)){/**< Alert Notification service UUID. */
            return "Alert Notification service";
        }else if(BLE_UUID_BATTERY_SERVICE.equals(uuidHead)){/**< Battery service UUID. */
            return "Battery service service";
        }else if(BLE_UUID_BLOOD_PRESSURE_SERVICE.equals(uuidHead)){/**< Blood Pressure service UUID. */
            return "Blood Pressure service";
        }else if(BLE_UUID_CURRENT_TIME_SERVICE.equals(uuidHead)){/**< Current Time service UUID. */
            return "Current Time service";
        }else if(BLE_UUID_CYCLING_SPEED_AND_CADENCE.equals(uuidHead)){ /**< Cycling Speed and Cadence service UUID. */
            return "Cycling Speed and Cadence service";
        }else if(BLE_UUID_DEVICE_INFORMATION_SERVICE.equals(uuidHead)){/**< Device Information service UUID. */
            return "Device Information service";
        }else if(BLE_UUID_GLUCOSE_SERVICE.equals(uuidHead)){/**< Glucose service UUID. */
            return "Glucose service";
        }else if(BLE_UUID_HEALTH_THERMOMETER_SERVICE.equals(uuidHead)){/**< Health Thermometer service UUID. */
            return "ealth Thermometer service";
        }else if(BLE_UUID_HEART_RATE_SERVICE.equals(uuidHead)){/**< Heart Rate service UUID. */
            return "Heart Rate service";
        }else if(BLE_UUID_HUMAN_INTERFACE_DEVICE_SERVICE.equals(uuidHead)){/**< Human Interface Device service UUID. */
            return "Human Interface Device service";
        }else if(BLE_UUID_IMMEDIATE_ALERT_SERVICE.equals(uuidHead)){/**< Immediate Alert service UUID. */
            return "Immediate Alert service";
        }else if(BLE_UUID_LINK_LOSS_SERVICE.equals(uuidHead)){/**< Link Loss service UUID. */
            return "Link Loss service";
        }else if(BLE_UUID_NEXT_DST_CHANGE_SERVICE.equals(uuidHead)){/**< Next Dst Change service UUID. */
            return "Next Dst Change service";
        }else if(BLE_UUID_PHONE_ALERT_STATUS_SERVICE.equals(uuidHead)){/**< Phone Alert Status service UUID. */
            return "Phone Alert Status service";
        }else if(BLE_UUID_REFERENCE_TIME_UPDATE_SERVICE.equals(uuidHead)){ /**< Reference Time Update service UUID. */
            return "Reference Time Update service";
        }else if(BLE_UUID_RUNNING_SPEED_AND_CADENCE.equals(uuidHead)){/**< Running Speed and Cadence service UUID. */
            return "Running Speed and Cadence service";
        }else if(BLE_UUID_SCAN_PARAMETERS_SERVICE.equals(uuidHead)){/**< Scan Parameters service UUID. */
            return "Scan Parameters service";
        }else if(BLE_UUID_TX_POWER_SERVICE.equals(uuidHead)){/**< TX Power service UUID. */
            return "TX Power service";
        }

        return null;
    }

    public static String getBLEPorperties(int porperties){

        StringBuilder sb = new StringBuilder();
        if((porperties & 0x01) != 0){
            sb.append("Broadcast  ");
        }else if((porperties & 0x02) != 0){
            sb.append("Read  ");
        }else if((porperties & 0x04) != 0){
            sb.append("WritWithoutResponse  ");
        }else if((porperties & 0x08) != 0){
            sb.append("Write  ");
        }else if((porperties & 0x10) != 0){
            sb.append("Notify  ");
        }else if((porperties & 0x20) != 0){
            sb.append("Indicate  ");
        }else if((porperties & 0x40) != 0){
            sb.append("AuthenticatedSignedWrites  ");
        }else if((porperties & 0x80) != 0){
            sb.append("ExtendedProperties  ");
        }

        return sb.toString();
    }

    public static String getBLECharactorsticType(String uuidHead){

        if(BLE_UUID_DEVICE_NAME_CHARACTERISTIC.equals(uuidHead)){
            return "Device Name";
        }else if(BLE_UUID_APPEARACE_CHARACTERISTIC.equals(uuidHead)){
            return "Appearace";
        }else if(BLE_UUID_PERIPHERAL_PRIVACY_FLAG_CHARACTERISTIC.equals(uuidHead)){
            return "Peripheral Privacy Flag";
        }else if(BLE_UUID_RECONNECTION_ADDRESS_CHARACTERISTIC.equals(uuidHead)){
            return "Reconnection Address";
        }else if(BLE_UUID_PERIPHERAL_PREFERRED_CONNECTION_PARAMETERS_CHARACTERISTIC.equals(uuidHead)){
            return "Peripheral Preferred Connection Parameters";
        }else if(BLE_UUID_SERVICE_CHANGED_CHARACTERISTIC.equals(uuidHead)){
            return "Service Changed";
        }

        return "";
    }

    public static String getBLECharactorsticDescripter(String uuidHead){
        if(BLE_UUID_CHARACTERISTIC_EXTENDED_PROPERTIES_CHARACTERISTIC.equals(uuidHead)){
            return "Extended Properties";
        }else if(BLE_UUID_CHARACTERISTIC_USER_DESCRIPTION_CHARACTERISTIC.equals(uuidHead)){
            return "User Description";
        }else if(BLE_UUID_CHARACTERISTIC_CLIENT_CONFIGURATION_CHARACTERISTIC.equals(uuidHead)){
            return "Client Configuration";
        }else if(BLE_UUID_CHARACTRRISTIC_SERVER_CONFIGURATION_CHARACTERISTIC.equals(uuidHead)){
            return "Server Configuration";
        }else if(BLE_UUID_CHARACTERISTIC_FORMAT_CHARACTERISTIC.equals(uuidHead)){
            return "Format";
        }else if(BLE_UUID_CHARACTERISTIC_AGGREGATE_FORMAT_CHARACTERISTIC.equals(uuidHead)){
            return "Aggregate Fromat";
        }
        return "";
    }
}
