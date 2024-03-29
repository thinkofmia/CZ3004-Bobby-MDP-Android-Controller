package com.example.mdpandroidcontrol;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import java.nio.charset.Charset;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements SensorEventListener{

    private static final String TAG = "MainActivity";

    boolean tiltNavi;
    private SensorManager sensorManager;
    private Sensor sensor;
    Switch tiltSwitch;

    static String connectedDevice;
    BluetoothDevice myBluetoothConnectionDevice;
    boolean connectedState;
    boolean currentActivity;
    Intent connectIntent;
    TextView connectionStatusBox;
    TextView mdfString;
    TextView obsString;

    TextView obstacleView;
    TextView mdfView;


    //UUID
    private static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    //Map Variables
    mapGridView mapView;
    final Button[][] mapPos = new Button[20][15];//Create map arrays of buttons

    //Data Type to send
    String sendStartExploration = "EX"; // Android > Algo: Start Exploration
    String sendStartFastest = "FP"; // Android > Algo: Start Fastest Path
    String sendManualControl = "control";
    String sendWaypoint = "waypoint"; // Android > Algo: Set Waypoint
    final String sendCustomText = "";
    String sendGetDirection = "getDirection";
    String sendTurnLeft = "A"; // Android > Arduino: Go Left
    String sendTurnRight = "D"; // Android > Arduino: Go Right
    String sendMoveForward = "W"; // Android > Arduino: Go Forward
    String sendMoveBack = "S"; // Android > Arduino: Go Back
    String SendArena = "SA"; // Android > Algo: Send MDF String
    String sendMapRequest = "mapstatus";
    String sendStartCoord = "startcoord";
    String sendCallibrateCode = "C";

    //Modes
    String currentMode = "None";
    String mapMode = "Auto";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("debugMsgs", "onCreate"); //Create a debug message when the app is created

        connectedDevice = null;
        connectedState = false;
        currentActivity = true;

        tiltNavi = false;

        //DECLARING SENSOR MANAGER AND SENSOR TYPE
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        //REGISTER TILT MOTION SENSOR
        sensorManager.registerListener((SensorEventListener) this, sensor, SensorManager.SENSOR_DELAY_NORMAL);

        connectionStatusBox = findViewById(R.id.bluetoothStatus);

        //Map variable
        mapView = new mapGridView();

        //Text variables
        //final EditText customInput = findViewById(R.id.editableInput);//Find the input text
        TextView positionText = findViewById(R.id.positionText);//Find the debug text for the position
        TextView statusText = findViewById(R.id.statusText);//Find the debug text for the status
        obstacleView = findViewById(R.id.obstacleview);//Find the obstacle view box
        TextView imageStringView = findViewById(R.id.imageStringView);//Find the image string view box
        mdfView = findViewById(R.id.exView);//Find the mdf view box

        //Buttons variables
        //final Button sendInput = findViewById(R.id.sendInput);//Find the send input button
        final ImageButton r_button = findViewById(R.id.button_right);//Find the right button
        final ImageButton l_button = findViewById(R.id.button_left);//Find the left button
        final ImageButton u_button = findViewById(R.id.button_up);//Find the up button
        final ImageButton d_button = findViewById(R.id.button_down);//Find the down button
        final Button str1_button = findViewById(R.id.predefinedStr1);//Find the string button 1
        final Button str2_button = findViewById(R.id.predefinedStr2);//Find the string button 2
        final Button waypoint_button = findViewById(R.id.button_waypoint);//Find the waypoint button
        final Button startcoord_button = findViewById(R.id.button_startcoord);//Find the start coordinates button
        final Button sp_button = findViewById(R.id.spbutton);// Find the shortest path button
        final Button explore_button = findViewById(R.id.exploreButton);// Find the explore button
        final Button auto_button = findViewById(R.id.button_auto);//Find the auto button
        final Button manual_button = findViewById(R.id.button_manual);//Find the manual button
        final Button calli_button = findViewById(R.id.callibrateButton);//Find the callibrate button

        //tilt switch
        tiltSwitch = findViewById(R.id.tiltSwitch);

        //mdf String text view
        mdfString = findViewById(R.id.obstacleview);

        //Map variables
        //Where [rows][columns]
        mapPos[0][0] = findViewById(R.id.position00);//Save the position 00
        mapPos[0][1] = findViewById(R.id.position01);//Save the position 01
        mapPos[0][2] = findViewById(R.id.position02);//Save the position 02
        mapPos[0][3] = findViewById(R.id.position03);//Save the position 03
        mapPos[0][4] = findViewById(R.id.position04);//Save the position 04
        mapPos[0][5] = findViewById(R.id.position05);//Save the position 05
        mapPos[0][6] = findViewById(R.id.position06);//Save the position 06
        mapPos[0][7] = findViewById(R.id.position07);//Save the position 07
        mapPos[0][8] = findViewById(R.id.position08);//Save the position 08
        mapPos[0][9] = findViewById(R.id.position09);//Save the position 09
        mapPos[0][10] = findViewById(R.id.position0A);//Save the position 0A
        mapPos[0][11] = findViewById(R.id.position0B);//Save the position 0B
        mapPos[0][12] = findViewById(R.id.position0C);//Save the position 0C
        mapPos[0][13] = findViewById(R.id.position0D);//Save the position 0D
        mapPos[0][14] = findViewById(R.id.position0E);//Save the position 0E
        mapPos[1][0] = findViewById(R.id.position10);//Save the position 10
        mapPos[1][1] = findViewById(R.id.position11);//Save the position 11
        mapPos[1][2] = findViewById(R.id.position12);//Save the position 12
        mapPos[1][3] = findViewById(R.id.position13);//Save the position 13
        mapPos[1][4] = findViewById(R.id.position14);//Save the position 14
        mapPos[1][5] = findViewById(R.id.position15);//Save the position 15
        mapPos[1][6] = findViewById(R.id.position16);//Save the position 16
        mapPos[1][7] = findViewById(R.id.position17);//Save the position 17
        mapPos[1][8] = findViewById(R.id.position18);//Save the position 18
        mapPos[1][9] = findViewById(R.id.position19);//Save the position 19
        mapPos[1][10] = findViewById(R.id.position1A);//Save the position 1A
        mapPos[1][11] = findViewById(R.id.position1B);//Save the position 1B
        mapPos[1][12] = findViewById(R.id.position1C);//Save the position 1C
        mapPos[1][13] = findViewById(R.id.position1D);//Save the position 1D
        mapPos[1][14] = findViewById(R.id.position1E);//Save the position 1E
        mapPos[2][0] = findViewById(R.id.position20);//Save the position 20
        mapPos[2][1] = findViewById(R.id.position21);//Save the position 21
        mapPos[2][2] = findViewById(R.id.position22);//Save the position 22
        mapPos[2][3] = findViewById(R.id.position23);//Save the position 23
        mapPos[2][4] = findViewById(R.id.position24);//Save the position 24
        mapPos[2][5] = findViewById(R.id.position25);//Save the position 25
        mapPos[2][6] = findViewById(R.id.position26);//Save the position 26
        mapPos[2][7] = findViewById(R.id.position27);//Save the position 27
        mapPos[2][8] = findViewById(R.id.position28);//Save the position 28
        mapPos[2][9] = findViewById(R.id.position29);//Save the position 29
        mapPos[2][10] = findViewById(R.id.position2A);//Save the position 2A
        mapPos[2][11] = findViewById(R.id.position2B);//Save the position 2B
        mapPos[2][12] = findViewById(R.id.position2C);//Save the position 2C
        mapPos[2][13] = findViewById(R.id.position2D);//Save the position 2D
        mapPos[2][14] = findViewById(R.id.position2E);//Save the position 2E
        mapPos[3][0] = findViewById(R.id.position30);//Save the position 30
        mapPos[3][1] = findViewById(R.id.position31);//Save the position 31
        mapPos[3][2] = findViewById(R.id.position32);//Save the position 32
        mapPos[3][3] = findViewById(R.id.position33);//Save the position 33
        mapPos[3][4] = findViewById(R.id.position34);//Save the position 34
        mapPos[3][5] = findViewById(R.id.position35);//Save the position 35
        mapPos[3][6] = findViewById(R.id.position36);//Save the position 36
        mapPos[3][7] = findViewById(R.id.position37);//Save the position 37
        mapPos[3][8] = findViewById(R.id.position38);//Save the position 38
        mapPos[3][9] = findViewById(R.id.position39);//Save the position 39
        mapPos[3][10] = findViewById(R.id.position3A);//Save the position 3A
        mapPos[3][11] = findViewById(R.id.position3B);//Save the position 3B
        mapPos[3][12] = findViewById(R.id.position3C);//Save the position 3C
        mapPos[3][13] = findViewById(R.id.position3D);//Save the position 3D
        mapPos[3][14] = findViewById(R.id.position3E);//Save the position 3E
        mapPos[4][0] = findViewById(R.id.position40);//Save the position 40
        mapPos[4][1] = findViewById(R.id.position41);//Save the position 41
        mapPos[4][2] = findViewById(R.id.position42);//Save the position 42
        mapPos[4][3] = findViewById(R.id.position43);//Save the position 43
        mapPos[4][4] = findViewById(R.id.position44);//Save the position 44
        mapPos[4][5] = findViewById(R.id.position45);//Save the position 45
        mapPos[4][6] = findViewById(R.id.position46);//Save the position 46
        mapPos[4][7] = findViewById(R.id.position47);//Save the position 47
        mapPos[4][8] = findViewById(R.id.position48);//Save the position 48
        mapPos[4][9] = findViewById(R.id.position49);//Save the position 49
        mapPos[4][10] = findViewById(R.id.position4A);//Save the position 4A
        mapPos[4][11] = findViewById(R.id.position4B);//Save the position 4B
        mapPos[4][12] = findViewById(R.id.position4C);//Save the position 4C
        mapPos[4][13] = findViewById(R.id.position4D);//Save the position 4D
        mapPos[4][14] = findViewById(R.id.position4E);//Save the position 4E
        mapPos[5][0] = findViewById(R.id.position50);//Save the position 50
        mapPos[5][1] = findViewById(R.id.position51);//Save the position 51
        mapPos[5][2] = findViewById(R.id.position52);//Save the position 52
        mapPos[5][3] = findViewById(R.id.position53);//Save the position 53
        mapPos[5][4] = findViewById(R.id.position54);//Save the position 54
        mapPos[5][5] = findViewById(R.id.position55);//Save the position 55
        mapPos[5][6] = findViewById(R.id.position56);//Save the position 56
        mapPos[5][7] = findViewById(R.id.position57);//Save the position 57
        mapPos[5][8] = findViewById(R.id.position58);//Save the position 58
        mapPos[5][9] = findViewById(R.id.position59);//Save the position 59
        mapPos[5][10] = findViewById(R.id.position5A);//Save the position 5A
        mapPos[5][11] = findViewById(R.id.position5B);//Save the position 5B
        mapPos[5][12] = findViewById(R.id.position5C);//Save the position 5C
        mapPos[5][13] = findViewById(R.id.position5D);//Save the position 5D
        mapPos[5][14] = findViewById(R.id.position5E);//Save the position 5E
        mapPos[6][0] = findViewById(R.id.position60);//Save the position 60
        mapPos[6][1] = findViewById(R.id.position61);//Save the position 61
        mapPos[6][2] = findViewById(R.id.position62);//Save the position 62
        mapPos[6][3] = findViewById(R.id.position63);//Save the position 63
        mapPos[6][4] = findViewById(R.id.position64);//Save the position 64
        mapPos[6][5] = findViewById(R.id.position65);//Save the position 65
        mapPos[6][6] = findViewById(R.id.position66);//Save the position 66
        mapPos[6][7] = findViewById(R.id.position67);//Save the position 67
        mapPos[6][8] = findViewById(R.id.position68);//Save the position 68
        mapPos[6][9] = findViewById(R.id.position69);//Save the position 69
        mapPos[6][10] = findViewById(R.id.position6A);//Save the position 6A
        mapPos[6][11] = findViewById(R.id.position6B);//Save the position 6B
        mapPos[6][12] = findViewById(R.id.position6C);//Save the position 6C
        mapPos[6][13] = findViewById(R.id.position6D);//Save the position 6D
        mapPos[6][14] = findViewById(R.id.position6E);//Save the position 6E
        mapPos[7][0] = findViewById(R.id.position70);//Save the position 70
        mapPos[7][1] = findViewById(R.id.position71);//Save the position 71
        mapPos[7][2] = findViewById(R.id.position72);//Save the position 72
        mapPos[7][3] = findViewById(R.id.position73);//Save the position 73
        mapPos[7][4] = findViewById(R.id.position74);//Save the position 74
        mapPos[7][5] = findViewById(R.id.position75);//Save the position 75
        mapPos[7][6] = findViewById(R.id.position76);//Save the position 76
        mapPos[7][7] = findViewById(R.id.position77);//Save the position 77
        mapPos[7][8] = findViewById(R.id.position78);//Save the position 78
        mapPos[7][9] = findViewById(R.id.position79);//Save the position 79
        mapPos[7][10] = findViewById(R.id.position7A);//Save the position 7A
        mapPos[7][11] = findViewById(R.id.position7B);//Save the position 7B
        mapPos[7][12] = findViewById(R.id.position7C);//Save the position 7C
        mapPos[7][13] = findViewById(R.id.position7D);//Save the position 7D
        mapPos[7][14] = findViewById(R.id.position7E);//Save the position 7E
        mapPos[8][0] = findViewById(R.id.position80);//Save the position 80
        mapPos[8][1] = findViewById(R.id.position81);//Save the position 81
        mapPos[8][2] = findViewById(R.id.position82);//Save the position 82
        mapPos[8][3] = findViewById(R.id.position83);//Save the position 83
        mapPos[8][4] = findViewById(R.id.position84);//Save the position 84
        mapPos[8][5] = findViewById(R.id.position85);//Save the position 85
        mapPos[8][6] = findViewById(R.id.position86);//Save the position 86
        mapPos[8][7] = findViewById(R.id.position87);//Save the position 87
        mapPos[8][8] = findViewById(R.id.position88);//Save the position 88
        mapPos[8][9] = findViewById(R.id.position89);//Save the position 89
        mapPos[8][10] = findViewById(R.id.position8A);//Save the position 8A
        mapPos[8][11] = findViewById(R.id.position8B);//Save the position 8B
        mapPos[8][12] = findViewById(R.id.position8C);//Save the position 8C
        mapPos[8][13] = findViewById(R.id.position8D);//Save the position 8D
        mapPos[8][14] = findViewById(R.id.position8E);//Save the position 8E
        mapPos[9][0] = findViewById(R.id.position90);//Save the position 90
        mapPos[9][1] = findViewById(R.id.position91);//Save the position 91
        mapPos[9][2] = findViewById(R.id.position92);//Save the position 92
        mapPos[9][3] = findViewById(R.id.position93);//Save the position 93
        mapPos[9][4] = findViewById(R.id.position94);//Save the position 94
        mapPos[9][5] = findViewById(R.id.position95);//Save the position 95
        mapPos[9][6] = findViewById(R.id.position96);//Save the position 96
        mapPos[9][7] = findViewById(R.id.position97);//Save the position 97
        mapPos[9][8] = findViewById(R.id.position98);//Save the position 98
        mapPos[9][9] = findViewById(R.id.position99);//Save the position 99
        mapPos[9][10] = findViewById(R.id.position9A);//Save the position 9A
        mapPos[9][11] = findViewById(R.id.position9B);//Save the position 9B
        mapPos[9][12] = findViewById(R.id.position9C);//Save the position 9C
        mapPos[9][13] = findViewById(R.id.position9D);//Save the position 9D
        mapPos[9][14] = findViewById(R.id.position9E);//Save the position 9E
        mapPos[10][0] = findViewById(R.id.positionA0);//Save the position A0
        mapPos[10][1] = findViewById(R.id.positionA1);//Save the position A1
        mapPos[10][2] = findViewById(R.id.positionA2);//Save the position A2
        mapPos[10][3] = findViewById(R.id.positionA3);//Save the position A3
        mapPos[10][4] = findViewById(R.id.positionA4);//Save the position A4
        mapPos[10][5] = findViewById(R.id.positionA5);//Save the position A5
        mapPos[10][6] = findViewById(R.id.positionA6);//Save the position A6
        mapPos[10][7] = findViewById(R.id.positionA7);//Save the position A7
        mapPos[10][8] = findViewById(R.id.positionA8);//Save the position A8
        mapPos[10][9] = findViewById(R.id.positionA9);//Save the position A9
        mapPos[10][10] = findViewById(R.id.positionAA);//Save the position AA
        mapPos[10][11] = findViewById(R.id.positionAB);//Save the position AB
        mapPos[10][12] = findViewById(R.id.positionAC);//Save the position AC
        mapPos[10][13] = findViewById(R.id.positionAD);//Save the position AD
        mapPos[10][14] = findViewById(R.id.positionAE);//Save the position AE
        mapPos[11][0] = findViewById(R.id.positionB0);//Save the position B0
        mapPos[11][1] = findViewById(R.id.positionB1);//Save the position B1
        mapPos[11][2] = findViewById(R.id.positionB2);//Save the position B2
        mapPos[11][3] = findViewById(R.id.positionB3);//Save the position B3
        mapPos[11][4] = findViewById(R.id.positionB4);//Save the position B4
        mapPos[11][5] = findViewById(R.id.positionB5);//Save the position B5
        mapPos[11][6] = findViewById(R.id.positionB6);//Save the position B6
        mapPos[11][7] = findViewById(R.id.positionB7);//Save the position B7
        mapPos[11][8] = findViewById(R.id.positionB8);//Save the position B8
        mapPos[11][9] = findViewById(R.id.positionB9);//Save the position B9
        mapPos[11][10] = findViewById(R.id.positionBA);//Save the position BA
        mapPos[11][11] = findViewById(R.id.positionBB);//Save the position BB
        mapPos[11][12] = findViewById(R.id.positionBC);//Save the position BC
        mapPos[11][13] = findViewById(R.id.positionBD);//Save the position BD
        mapPos[11][14] = findViewById(R.id.positionBE);//Save the position BE
        mapPos[12][0] = findViewById(R.id.positionC0);//Save the position C0
        mapPos[12][1] = findViewById(R.id.positionC1);//Save the position C1
        mapPos[12][2] = findViewById(R.id.positionC2);//Save the position C2
        mapPos[12][3] = findViewById(R.id.positionC3);//Save the position C3
        mapPos[12][4] = findViewById(R.id.positionC4);//Save the position C4
        mapPos[12][5] = findViewById(R.id.positionC5);//Save the position C5
        mapPos[12][6] = findViewById(R.id.positionC6);//Save the position C6
        mapPos[12][7] = findViewById(R.id.positionC7);//Save the position C7
        mapPos[12][8] = findViewById(R.id.positionC8);//Save the position C8
        mapPos[12][9] = findViewById(R.id.positionC9);//Save the position C9
        mapPos[12][10] = findViewById(R.id.positionCA);//Save the position CA
        mapPos[12][11] = findViewById(R.id.positionCB);//Save the position CB
        mapPos[12][12] = findViewById(R.id.positionCC);//Save the position CC
        mapPos[12][13] = findViewById(R.id.positionCD);//Save the position CD
        mapPos[12][14] = findViewById(R.id.positionCE);//Save the position CE
        mapPos[13][0] = findViewById(R.id.positionD0);//Save the position D0
        mapPos[13][1] = findViewById(R.id.positionD1);//Save the position D1
        mapPos[13][2] = findViewById(R.id.positionD2);//Save the position D2
        mapPos[13][3] = findViewById(R.id.positionD3);//Save the position D3
        mapPos[13][4] = findViewById(R.id.positionD4);//Save the position D4
        mapPos[13][5] = findViewById(R.id.positionD5);//Save the position D5
        mapPos[13][6] = findViewById(R.id.positionD6);//Save the position D6
        mapPos[13][7] = findViewById(R.id.positionD7);//Save the position D7
        mapPos[13][8] = findViewById(R.id.positionD8);//Save the position D8
        mapPos[13][9] = findViewById(R.id.positionD9);//Save the position D9
        mapPos[13][10] = findViewById(R.id.positionDA);//Save the position DA
        mapPos[13][11] = findViewById(R.id.positionDB);//Save the position DB
        mapPos[13][12] = findViewById(R.id.positionDC);//Save the position DC
        mapPos[13][13] = findViewById(R.id.positionDD);//Save the position DD
        mapPos[13][14] = findViewById(R.id.positionDE);//Save the position DE
        mapPos[14][0] = findViewById(R.id.positionE0);//Save the position E0
        mapPos[14][1] = findViewById(R.id.positionE1);//Save the position E1
        mapPos[14][2] = findViewById(R.id.positionE2);//Save the position E2
        mapPos[14][3] = findViewById(R.id.positionE3);//Save the position E3
        mapPos[14][4] = findViewById(R.id.positionE4);//Save the position E4
        mapPos[14][5] = findViewById(R.id.positionE5);//Save the position E5
        mapPos[14][6] = findViewById(R.id.positionE6);//Save the position E6
        mapPos[14][7] = findViewById(R.id.positionE7);//Save the position E7
        mapPos[14][8] = findViewById(R.id.positionE8);//Save the position E8
        mapPos[14][9] = findViewById(R.id.positionE9);//Save the position E9
        mapPos[14][10] = findViewById(R.id.positionEA);//Save the position EA
        mapPos[14][11] = findViewById(R.id.positionEB);//Save the position EB
        mapPos[14][12] = findViewById(R.id.positionEC);//Save the position EC
        mapPos[14][13] = findViewById(R.id.positionED);//Save the position ED
        mapPos[14][14] = findViewById(R.id.positionEE);//Save the position EE
        mapPos[15][0] = findViewById(R.id.positionF0);//Save the position F0
        mapPos[15][1] = findViewById(R.id.positionF1);//Save the position F1
        mapPos[15][2] = findViewById(R.id.positionF2);//Save the position F2
        mapPos[15][3] = findViewById(R.id.positionF3);//Save the position F3
        mapPos[15][4] = findViewById(R.id.positionF4);//Save the position F4
        mapPos[15][5] = findViewById(R.id.positionF5);//Save the position F5
        mapPos[15][6] = findViewById(R.id.positionF6);//Save the position F6
        mapPos[15][7] = findViewById(R.id.positionF7);//Save the position F7
        mapPos[15][8] = findViewById(R.id.positionF8);//Save the position F8
        mapPos[15][9] = findViewById(R.id.positionF9);//Save the position F9
        mapPos[15][10] = findViewById(R.id.positionFA);//Save the position FA
        mapPos[15][11] = findViewById(R.id.positionFB);//Save the position FB
        mapPos[15][12] = findViewById(R.id.positionFC);//Save the position FC
        mapPos[15][13] = findViewById(R.id.positionFD);//Save the position FD
        mapPos[15][14] = findViewById(R.id.positionFE);//Save the position FE
        mapPos[16][0] = findViewById(R.id.positionG0);//Save the position G0
        mapPos[16][1] = findViewById(R.id.positionG1);//Save the position G1
        mapPos[16][2] = findViewById(R.id.positionG2);//Save the position G2
        mapPos[16][3] = findViewById(R.id.positionG3);//Save the position G3
        mapPos[16][4] = findViewById(R.id.positionG4);//Save the position G4
        mapPos[16][5] = findViewById(R.id.positionG5);//Save the position G5
        mapPos[16][6] = findViewById(R.id.positionG6);//Save the position G6
        mapPos[16][7] = findViewById(R.id.positionG7);//Save the position G7
        mapPos[16][8] = findViewById(R.id.positionG8);//Save the position G8
        mapPos[16][9] = findViewById(R.id.positionG9);//Save the position G9
        mapPos[16][10] = findViewById(R.id.positionGA);//Save the position GA
        mapPos[16][11] = findViewById(R.id.positionGB);//Save the position GB
        mapPos[16][12] = findViewById(R.id.positionGC);//Save the position GC
        mapPos[16][13] = findViewById(R.id.positionGD);//Save the position GD
        mapPos[16][14] = findViewById(R.id.positionGE);//Save the position GE
        mapPos[17][0] = findViewById(R.id.positionH0);//Save the position H0
        mapPos[17][1] = findViewById(R.id.positionH1);//Save the position H1
        mapPos[17][2] = findViewById(R.id.positionH2);//Save the position H2
        mapPos[17][3] = findViewById(R.id.positionH3);//Save the position H3
        mapPos[17][4] = findViewById(R.id.positionH4);//Save the position H4
        mapPos[17][5] = findViewById(R.id.positionH5);//Save the position H5
        mapPos[17][6] = findViewById(R.id.positionH6);//Save the position H6
        mapPos[17][7] = findViewById(R.id.positionH7);//Save the position H7
        mapPos[17][8] = findViewById(R.id.positionH8);//Save the position H8
        mapPos[17][9] = findViewById(R.id.positionH9);//Save the position H9
        mapPos[17][10] = findViewById(R.id.positionHA);//Save the position HA
        mapPos[17][11] = findViewById(R.id.positionHB);//Save the position HB
        mapPos[17][12] = findViewById(R.id.positionHC);//Save the position HC
        mapPos[17][13] = findViewById(R.id.positionHD);//Save the position HD
        mapPos[17][14] = findViewById(R.id.positionHE);//Save the position HE
        mapPos[18][0] = findViewById(R.id.positionI0);//Save the position I0
        mapPos[18][1] = findViewById(R.id.positionI1);//Save the position I1
        mapPos[18][2] = findViewById(R.id.positionI2);//Save the position I2
        mapPos[18][3] = findViewById(R.id.positionI3);//Save the position I3
        mapPos[18][4] = findViewById(R.id.positionI4);//Save the position I4
        mapPos[18][5] = findViewById(R.id.positionI5);//Save the position I5
        mapPos[18][6] = findViewById(R.id.positionI6);//Save the position I6
        mapPos[18][7] = findViewById(R.id.positionI7);//Save the position I7
        mapPos[18][8] = findViewById(R.id.positionI8);//Save the position I8
        mapPos[18][9] = findViewById(R.id.positionI9);//Save the position I9
        mapPos[18][10] = findViewById(R.id.positionIA);//Save the position IA
        mapPos[18][11] = findViewById(R.id.positionIB);//Save the position IB
        mapPos[18][12] = findViewById(R.id.positionIC);//Save the position IC
        mapPos[18][13] = findViewById(R.id.positionID);//Save the position ID
        mapPos[18][14] = findViewById(R.id.positionIE);//Save the position IE
        mapPos[19][0] = findViewById(R.id.positionJ0);//Save the position J0
        mapPos[19][1] = findViewById(R.id.positionJ1);//Save the position J1
        mapPos[19][2] = findViewById(R.id.positionJ2);//Save the position J2
        mapPos[19][3] = findViewById(R.id.positionJ3);//Save the position J3
        mapPos[19][4] = findViewById(R.id.positionJ4);//Save the position J4
        mapPos[19][5] = findViewById(R.id.positionJ5);//Save the position J5
        mapPos[19][6] = findViewById(R.id.positionJ6);//Save the position J6
        mapPos[19][7] = findViewById(R.id.positionJ7);//Save the position J7
        mapPos[19][8] = findViewById(R.id.positionJ8);//Save the position J8
        mapPos[19][9] = findViewById(R.id.positionJ9);//Save the position J9
        mapPos[19][10] = findViewById(R.id.positionJA);//Save the position JA
        mapPos[19][11] = findViewById(R.id.positionJB);//Save the position JB
        mapPos[19][12] = findViewById(R.id.positionJC);//Save the position JC
        mapPos[19][13] = findViewById(R.id.positionJD);//Save the position JD
        mapPos[19][14] = findViewById(R.id.positionJE);//Save the position JE


        updateMap();

        //Log.d("debugMsgs", "Sending Input: "+customInput.getText());//Create a debug message when the app is created

        //REGISTER BROADCAST RECEIVER FOR MAINTAINING BLUETOOTH CONNECTION
        LocalBroadcastManager.getInstance(this).registerReceiver(btConnectionReceiver, new IntentFilter("btConnectionStatus"));

        //REGISTER BROADCAST RECEIVER FOR IMCOMING MSG
        LocalBroadcastManager.getInstance(this).registerReceiver(incomingMsgReceiver, new IntentFilter("IncomingMsg"));

        //Onclick function for sendInput
        /*sendInput.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //CHECK IF CONNECTED TO DEVICE FIRST
                if (connectedDevice == null) {
                    Toast.makeText(MainActivity.this, "Please Connect to a Device First!!",
                            Toast.LENGTH_SHORT).show();
                } else {
                    //Get input text
                    final EditText customInput = findViewById(R.id.editableInput);//Find the input text
                    String inputMsg = customInput.getText().toString();
                    //Set code to sent
                    String sendThisCode = sendCustomText + "'" + inputMsg + "'";
                    //send the code to rpi
                    sendToRPi(sendThisCode);
                    //If input msg exists, send toast
                    if (inputMsg.length() > 0) {
                        Toast.makeText(MainActivity.this, "Sending: " + sendThisCode, Toast.LENGTH_SHORT).show();//Display send input toast
                        Log.d("debugMsgs", "Sending Input: " + customInput.getText());//Create a debug message when the input is transmitted
                    } else {//Else show false toast
                        Toast.makeText(MainActivity.this, "Please type something first! ", Toast.LENGTH_SHORT).show();//Display invalid input toast
                        Log.d("debugMsgs", "Invalid input. Sending failed. ");//Create a debug message when the input isn't found
                    }
                }
            }
        });*/

        //Onclick function for right button
        r_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                moveRight();
            }
        });

        //Onclick function for left button
        l_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                moveLeft();
            }
        });

        //Onclick function for up button
        u_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                moveUp();
            }
        });

        //Onclick function for down button
        d_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                moveDown();
            }
        });

        //Onclick function for str1 button
        str1_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                sendPredefinedStr(1);
            }
        });

        //Onclick function for str2 button
        str2_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                sendPredefinedStr(2);
            }
        });

        //Onclick function for callibrate button
        calli_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                sendCallibrate();
            }
        });

        //Onclick function for start coordinates button
        startcoord_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                currentMode = "Start Coordinates";//Set current mode to be choosing starting coordinates
                String textToDisplay = "Selecting starting coordinates...";
                //Display Toast
                //Toast.makeText(MainActivity.this, textToDisplay, Toast.LENGTH_SHORT).show();//Display toast
                //Update Status
                TextView statusText = findViewById(R.id.statusText);//Find the debug text for the status
                statusText.setText(textToDisplay);
            }
        });

        //Onclick function for way point button
        waypoint_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                currentMode = "Set Waypoint";//Set current mode to be setting waypoint
                String textToDisplay = "Selecting waypoint...";
                //Display Toast
                //Toast.makeText(MainActivity.this, textToDisplay, Toast.LENGTH_SHORT).show();//Display toast
                //Update Status
                TextView statusText = findViewById(R.id.statusText);//Find the debug text for the status
                statusText.setText(textToDisplay);
            }
        });

        //Onclick function for shortest path button
        sp_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                sendToRPi(sendStartFastest);
                //Set text to display
                String textToDisplay = "Sending: "+sendStartFastest;
                //Update Status
                TextView statusText = findViewById(R.id.statusText);//Find the debug text for the status
                statusText.setText(textToDisplay);
            }
        });

        //Onclick function for explore button
        explore_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                sendToRPi(sendStartExploration);
                //Set text to display
                String textToDisplay = "Sending: "+sendStartExploration;
                //Update Status
                TextView statusText = findViewById(R.id.statusText);//Find the debug text for the status
                statusText.setText(textToDisplay);
            }
        });

        //Onclick function for manual button
        manual_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //Set map mode to be manual
                mapMode = "Manual";
                //Update Map
                updateMap();
                //Display toast
                Toast.makeText(MainActivity.this, "Map Mode: Manual", Toast.LENGTH_SHORT).show();
            }
        });

        //Onclick function for auto button
        auto_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //Set map mode to be auto
                mapMode = "Auto";
                //Update Map
                updateMap();
                //Display toast
                Toast.makeText(MainActivity.this, "Map Mode: Auto", Toast.LENGTH_SHORT).show();
            }
        });

        //For loop for all the map tiles
        for (int y=0; y<mapView.getRows();y++){
            for (int x=0;x<mapView.getColumns();x++){
                final int finalX = x;
                final int finalY = y;
                mapPos[y][x].setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        String result = mapTileClicked(finalX, finalY);
                        currentMode = "";
                        if (result!="Do Nothing"){
                            //Display Toast
                            Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();//Display toast
                        }
                    }
                });

                }
            }

        //on click listener for tilt switch
        onClickTiltSwitch();

    }

    protected String mapTileClicked(int x, int y){
        //Swtich-case for current mode
        switch (currentMode){
            case "Set Waypoint":
                mapView.setWaypoint(x,y);//Set way point on the map
                if (mapMode=="Auto") updateMap();//Update map
                sendToRPi(sendWaypoint+"'|("+x+","+y+")'");//Send to bluetooth
                return "Waypoint set to ("+x+", "+y+")";
            case "Start Coordinates":
                mapView.setCoordinates(x,y);
                if (mapMode=="Auto") updateMap();//Update map
                sendToRPi(sendStartCoord+"'("+x+","+y+")'");//Send to bluetooth
                return "Start Coordinates set to ("+x+", "+y+")";
            default:
                return "Do Nothing";
        }

    }

    protected void sendCallibrate() {
        //Send Callibrate Code to rpi
        sendToRPi(sendCallibrateCode);
        String result = "Sending: " + sendCallibrateCode;
        //Update Status
        TextView statusText = findViewById(R.id.statusText);//Find the debug text for the status
        statusText.setText("Status: " + result.toString());
    }

    protected void sendPredefinedStr(int option){
            String result = "";
            String sendThisCode;
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            switch (option) {
                case 2:
                    //Sets string message
                    sendThisCode = sendCustomText + "'" + settings.getString("Str2", "No String Set").toString() + "'";
                    //send the code to rpi
                    sendToRPi(sendThisCode);
                    result = "Sending: " + sendThisCode;

                    break;
                case 1:
                default:
                    //Sets string message
                    sendThisCode = sendCustomText + "'" + settings.getString("Str1", "No String Set").toString() + "'";
                    //send the code to rpi
                    sendToRPi(sendThisCode);
                    result = "Sending: " + sendThisCode;
                    break;
            }
            //Display Toast
            //Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();//Display toast
            //Update Status
            TextView statusText = findViewById(R.id.statusText);//Find the debug text for the status
            statusText.setText("Status: " + result.toString());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings, menu);
        getMenuInflater().inflate(R.menu.bluetooth, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.item_preference:
                goToSettings();
                return true;

            case R.id.item_bluetooth:

                currentActivity = false;

                Intent intent = new Intent(MainActivity.this, Bluetooth.class);
                startActivity(intent);


        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    protected void onResume(){
        super.onResume();
        Log.d("debugMsgs", "onResume");//Create a debug message when the app is resumed

        currentActivity = true;

        //REGISTER TILT MOTION SENSOR
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);

        Log.d(" MainActivity:", "OnResume:" + connectedState);

        //CHECK FOR EXISTING CONNECTION
        if (connectedState) {
            Log.d(" MainAcitvity:", "OnResume1");

            //SET TEXTFIELD TO DEVICE NAME
            connectionStatusBox.setText(connectedDevice);
        } else {
            Log.d(" MainActivity:", "OnResume2");

            //SET TEXTFIELD TO NOT CONNECTED
            connectionStatusBox.setText(R.string.btStatusOffline);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("debugMsgs", "onStart");//Create a debug message when the app is started
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("debugMsgs", "onPause");//Create a debug message when the app is paused
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("debugMsgs", "onStop");//Create a debug message when the app is stopped
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("debugMsgs", "onDestroyed");//Create a debug message when the app is destroyed
    }

    protected void goToSettings(){
        Log.d("debugMsgs", "Going to settings");//Create a debug message when settings menu is clicked

        currentActivity = false;
        //Create intent
        Intent intent = new Intent(MainActivity.this, preferencesActivity.class);
        //Run settings
        startActivity(intent);
    }

    protected boolean updateMap(){
        Log.d("debugMsgs", "Updating map position");//Create a debug message when the map is updated

        //Variables
        TextView positionText = findViewById(R.id.positionText);//Find the debug text for the position
        int[][] currentMap = mapView.getMapper();//Gets the current map information

        /**
         * image ids:
         * -1: Explored
         * 0: Unexplored
         * 1: Up Arrow
         * 2: Down Arrow
         * 3: Right Arrow
         * 4: Left Arrow
         * 5: Go
         * 6: Six
         * 7: Seven
         * 8: Eight
         * 9: Nine
         * 10: Zero
         * 11: Alphabet V
         * 12: Alphabet W
         * 13: Alphabet X
         * 14: Alphabet Y
         * 15: Alphabet Z
         * 100: Trail
         * 20: Waypoint
         * anything else: obstacle
         */

        //For loop to run the map
        for (int y=0; y<mapView.getRows();y++){
            for (int x=0;x<mapView.getColumns();x++){
                //Condition to check if its within start or goal zone
                if (y<=2 && x<=2) mapPos[y][x].setText("S");//Label zone as S
                else if (y>=17 && x>=12) mapPos[y][x].setText("G");//Label zone as G
                else mapPos[y][x].setText("");//Clear Text
                //Set text size of buttons
                mapPos[y][x].setTextSize(8);
                switch (currentMap[y][x]){
                    case 0://Default Unexplored
                        //ViewCompat.setBackgroundTintList(mapPos[y][x], ContextCompat.getColorStateList(this, android.R.color.darker_gray));
                        mapPos[y][x].setBackgroundColor(0x7E404040);//Set the background color of the map to be white
                        break;
                    case -1://Explored
                        mapPos[y][x].setBackgroundColor(0x10000000);//Set the background color of the map to be white
                        break;
                    case 1://Obstacles: Arrow Up
                        mapPos[y][x].setBackgroundColor(0x70FF0000);//Set bg color of the map
                        mapPos[y][x].setText("1");//Set Text as ↑
                        break;
                    case 2://Obstacles: Arrow Down
                        mapPos[y][x].setBackgroundColor(0x70FF0000);//Set bg color of the map
                        mapPos[y][x].setText("2");//Set Text as ↓
                        break;
                    case 3://Obstacles: Arrow Right
                        mapPos[y][x].setBackgroundColor(0x70FF0000);//Set bg color of the map
                        mapPos[y][x].setText("3");//Set Text as ➞
                        break;
                    case 4://Obstacles: Arrow Left
                        mapPos[y][x].setBackgroundColor(0x70FF0000);//Set bg color of the map
                        mapPos[y][x].setText("4");//Set Text as ←
                        break;
                    case 5://Obstacles: Go
                        mapPos[y][x].setBackgroundColor(0x70FF0000);//Set bg color of the map
                        mapPos[y][x].setText("5");//Set Text as ⦿
                        break;
                    case 6://Obstacles: 6
                        mapPos[y][x].setBackgroundColor(0x70FF0000);//Set bg color of the map
                        mapPos[y][x].setText("6");//Set Text as 6
                        break;
                    case 7://Obstacles: 7
                        mapPos[y][x].setBackgroundColor(0x70FF0000);//Set bg color of the map
                        mapPos[y][x].setText("7");//Set Text as 7
                        break;
                    case 8://Obstacles: 8
                        mapPos[y][x].setBackgroundColor(0x70FF0000);//Set bg color of the map
                        mapPos[y][x].setText("8");//Set Text as 8
                        break;
                    case 9://Obstacles: 9
                        mapPos[y][x].setBackgroundColor(0x70FF0000);//Set bg color of the map
                        mapPos[y][x].setText("9");//Set Text as 9
                        break;
                    case 10://Obstacles: 10
                        mapPos[y][x].setBackgroundColor(0x70FF0000);//Set bg color of the map
                        mapPos[y][x].setText("10");//Set Text as 0
                        break;
                    case 11://Obstacles: V
                        mapPos[y][x].setBackgroundColor(0x70FF0000);//Set bg color of the map
                        mapPos[y][x].setText("11");//Set Text as V
                        break;
                    case 12://Obstacles: W
                        mapPos[y][x].setBackgroundColor(0x70FF0000);//Set bg color of the map
                        mapPos[y][x].setText("12");//Set Text as W
                        break;
                    case 13://Obstacles: X
                        mapPos[y][x].setBackgroundColor(0x70FF0000);//Set bg color of the map
                        mapPos[y][x].setText("13");//Set Text as X
                        break;
                    case 14://Obstacles: Y
                        mapPos[y][x].setBackgroundColor(0x70FF0000);//Set bg color of the map
                        mapPos[y][x].setText("14");//Set Text as Y
                        break;
                    case 15://Obstacles: Z
                        mapPos[y][x].setBackgroundColor(0x70FF0000);//Set bg color of the map
                        mapPos[y][x].setText("15");//Set Text as Z
                        break;
                    case 100://Trail
                        mapPos[y][x].setBackgroundColor(0x709DF9FF);//Set bg color of the map
                        //mapPos[y][x].setText("☼");//Set Text as ☼
                        //ViewCompat.setBackgroundTintList(mapPos[y][x], ContextCompat.getColorStateList(this, android.R.color.holo_blue_light));
                        break;
                    case 20: //Waypoint
                        mapPos[y][x].setBackgroundResource(R.mipmap.pokeball_background);
                        //mapPos[y][x].setBackgroundColor(0x70F0F000);//Set bg color of the map
                        //ViewCompat.setBackgroundTintList(mapPos[y][x], ContextCompat.getColorStateList(this, android.R.color.holo_blue_light));
                        break;
                    default:
                        //ViewCompat.setBackgroundTintList(mapPos[y][x], ContextCompat.getColorStateList(this, android.R.color.holo_blue_light));
                        //mapPos[y][x].setBackgroundColor(0xFF000000);//Set the background color of the map to be black
                        mapPos[y][x].setBackgroundResource(R.mipmap.blue_ship_north_background);
                        //mapPos[y][x].setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.bobby_up_button_background, null));
                        break;
                }

                //Set Goal and Start Points
                if (y<=2 && x<=2) mapPos[y][x].setBackgroundColor(0x7000FF00);//Color start zone
                else if (y>=17 && x>=12) mapPos[y][x].setBackgroundColor(0x70FF0000);//Color goal zone
            }
        }

        //Get dummy coordinates
        int[] coordinates = new int[2];
        coordinates[0] = mapView.getCoordinates()[0];//Get x-coordinate
        coordinates[1] = mapView.getCoordinates()[1];//Get y-coordinate

        //Clear Texts for the robot
        mapPos[coordinates[1]-1][coordinates[0]-1].setText("");//Top-Left of the robot
        mapPos[coordinates[1]-1][coordinates[0]].setText("");//Top of the robot
        mapPos[coordinates[1]-1][coordinates[0]+1].setText("");//Top-Right of the robot
        mapPos[coordinates[1]][coordinates[0]-1].setText("");//Left of the robot
        mapPos[coordinates[1]][coordinates[0]].setText("");//Center of the934b00
        mapPos[coordinates[1]][coordinates[0]+1].setText("");//Right of the robot
        mapPos[coordinates[1]+1][coordinates[0]-1].setText("");//Bottom-Left of the robot
        mapPos[coordinates[1]+1][coordinates[0]].setText("");//Bottom of the robot
        mapPos[coordinates[1]+1][coordinates[0]+1].setText("");//Bottom-Right of the robot

        //Set the map position of the robot:
        //mapPos[coordinates[1]-1][coordinates[0]-1].setBackgroundColor(0xFF934b00);//Top-Left of the robot
        //mapPos[coordinates[1]-1][coordinates[0]].setBackgroundColor(0xFF934b00);//Top of the robot
        //mapPos[coordinates[1]-1][coordinates[0]+1].setBackgroundColor(0xFF934b00);//Top-Right of the robot
        //mapPos[coordinates[1]][coordinates[0]-1].setBackgroundColor(0xFF934b00);//Left of the robot
        //mapPos[coordinates[1]][coordinates[0]].setBackgroundColor(0xFF934b00);//Center of the robot
        //mapPos[coordinates[1]][coordinates[0]+1].setBackgroundColor(0xFF934b00);//Right of the robot
        //mapPos[coordinates[1]+1][coordinates[0]-1].setBackgroundColor(0xFF934b00);//Bottom-Left of the robot
        //mapPos[coordinates[1]+1][coordinates[0]].setBackgroundColor(0xFF934b00);//Bottom of the robot
        //mapPos[coordinates[1]+1][coordinates[0]+1].setBackgroundColor(0xFF934b00);//Bottom-Right of the robot

        //Get direction of the robot
        String direction = mapView.getDirection();//Get direction of the robot
        //Set the direction of the robot
        switch (direction){
            case "North":
                //Set Arrow
                //mapPos[coordinates[1]+1][coordinates[0]].setText("▲");
                //Set2e2d2b
                //mapPos[coordinates[1]+1][coordinates[0]].setBackgroundColor(0xFF2e2d2b);//Bottom of the robot
                //Set Bobby Sprite
                mapPos[coordinates[1]+1][coordinates[0]-1].setBackgroundResource(R.mipmap.bobbyn00_background);//Top-Left of the robot
                mapPos[coordinates[1]][coordinates[0]-1].setBackgroundResource(R.mipmap.bobbyn01_background);//Left of the robot
                mapPos[coordinates[1]-1][coordinates[0]-1].setBackgroundResource(R.mipmap.bobbyn02_background);//Bottom-Left of the robot
                mapPos[coordinates[1]+1][coordinates[0]].setBackgroundResource(R.mipmap.bobbyn10_background);//Top of the robot
                mapPos[coordinates[1]][coordinates[0]].setBackgroundResource(R.mipmap.bobbyn11_background);//Center of the robot
                mapPos[coordinates[1]-1][coordinates[0]].setBackgroundResource(R.mipmap.bobbyn12_background);//Bottom of the robot
                mapPos[coordinates[1]+1][coordinates[0]+1].setBackgroundResource(R.mipmap.bobbyn20_background);//Top-Right of the robot
                mapPos[coordinates[1]][coordinates[0]+1].setBackgroundResource(R.mipmap.bobbyn21_background);//Right of the robot
                mapPos[coordinates[1]-1][coordinates[0]+1].setBackgroundResource(R.mipmap.bobbyn22_background);//Bottom-Right of the robot
                break;
            case "South":
                //Set Bobby Sprite
                mapPos[coordinates[1]+1][coordinates[0]-1].setBackgroundResource(R.mipmap.bobbys00_background);//Top-Left of the robot
                mapPos[coordinates[1]][coordinates[0]-1].setBackgroundResource(R.mipmap.bobbys01_background);//Left of the robot
                mapPos[coordinates[1]-1][coordinates[0]-1].setBackgroundResource(R.mipmap.bobbys02_background);//Bottom-Left of the robot
                mapPos[coordinates[1]+1][coordinates[0]].setBackgroundResource(R.mipmap.bobbys10_background);//Top of the robot
                mapPos[coordinates[1]][coordinates[0]].setBackgroundResource(R.mipmap.bobbys11_background);//Center of the robot
                mapPos[coordinates[1]-1][coordinates[0]].setBackgroundResource(R.mipmap.bobbys12_background);//Bottom of the robot
                mapPos[coordinates[1]+1][coordinates[0]+1].setBackgroundResource(R.mipmap.bobbys20_background);//Top-Right of the robot
                mapPos[coordinates[1]][coordinates[0]+1].setBackgroundResource(R.mipmap.bobbys21_background);//Right of the robot
                mapPos[coordinates[1]-1][coordinates[0]+1].setBackgroundResource(R.mipmap.bobbys22_background);//Bottom-Right of the robot
                //Set Arrow
                //mapPos[coordinates[1]-1][coordinates[0]].setText("▼");
                //Set Color
                //mapPos[coordinates[1]-1][coordinates[0]].setBackgroundColor(0xFF2e2d2b);//Top of the robot
                break;
            case "East":
                //Set Arrow
                //mapPos[coordinates[1]][coordinates[0]+1].setText("▶");
                //Set Color
                //mapPos[coordinates[1]][coordinates[0]+1].setBackgroundColor(0xFF2e2d2b);//Right of the robot
                //Set Bobby Sprite
                mapPos[coordinates[1]+1][coordinates[0]-1].setBackgroundResource(R.mipmap.bobbye00_background);//Top-Left of the robot
                mapPos[coordinates[1]][coordinates[0]-1].setBackgroundResource(R.mipmap.bobbye01_background);//Left of the robot
                mapPos[coordinates[1]-1][coordinates[0]-1].setBackgroundResource(R.mipmap.bobbye02_background);//Bottom-Left of the robot
                mapPos[coordinates[1]+1][coordinates[0]].setBackgroundResource(R.mipmap.bobbye10_background);//Top of the robot
                mapPos[coordinates[1]][coordinates[0]].setBackgroundResource(R.mipmap.bobbye11_background);//Center of the robot
                mapPos[coordinates[1]-1][coordinates[0]].setBackgroundResource(R.mipmap.bobbye12_background);//Bottom of the robot
                mapPos[coordinates[1]+1][coordinates[0]+1].setBackgroundResource(R.mipmap.bobbye20_background);//Top-Right of the robot
                mapPos[coordinates[1]][coordinates[0]+1].setBackgroundResource(R.mipmap.bobbye21_background);//Right of the robot
                mapPos[coordinates[1]-1][coordinates[0]+1].setBackgroundResource(R.mipmap.bobbye22_background);//Bottom-Right of the robot
                break;
            case "West":
                //Set Arrow
                //mapPos[coordinates[1]][coordinates[0]-1].setText("◀");
                //Set Color
                //mapPos[coordinates[1]][coordinates[0]-1].setBackgroundColor(0xFF2e2d2b);//Left of the robot
                //Set Bobby Sprite
                mapPos[coordinates[1]+1][coordinates[0]-1].setBackgroundResource(R.mipmap.bobbyw00_background);//Top-Left of the robot
                mapPos[coordinates[1]][coordinates[0]-1].setBackgroundResource(R.mipmap.bobbyw01_background);//Left of the robot
                mapPos[coordinates[1]-1][coordinates[0]-1].setBackgroundResource(R.mipmap.bobbyw02_background);//Bottom-Left of the robot
                mapPos[coordinates[1]+1][coordinates[0]].setBackgroundResource(R.mipmap.bobbyw10_background);//Top of the robot
                mapPos[coordinates[1]][coordinates[0]].setBackgroundResource(R.mipmap.bobbyw11_background);//Center of the robot
                mapPos[coordinates[1]-1][coordinates[0]].setBackgroundResource(R.mipmap.bobbyw12_background);//Bottom of the robot
                mapPos[coordinates[1]+1][coordinates[0]+1].setBackgroundResource(R.mipmap.bobbyw20_background);//Top-Right of the robot
                mapPos[coordinates[1]][coordinates[0]+1].setBackgroundResource(R.mipmap.bobbyw21_background);//Right of the robot
                mapPos[coordinates[1]-1][coordinates[0]+1].setBackgroundResource(R.mipmap.bobbyw22_background);//Bottom-Right of the robot
                break;
        }


        //Setting text view for positions
        String coordinates_str = "("+coordinates[0]+", "+coordinates[1]+") ";
        positionText.setText(coordinates_str);
        return true;
    }

    protected void moveLeft(){
            //When on click, move left
            //Update Map
            String result = mapView.moveRobotLeft();
            if (mapMode=="Auto") updateMap();
            //send the command to the Raspberry pi
            sendToRPi(sendTurnLeft);
            //Set message
            result = "Sending: " + sendTurnLeft;
            //Display Toast
            //Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();//Display toast
            //Update Status
            TextView statusText = findViewById(R.id.statusText);//Find the debug text for the status
            statusText.setText("Status: "+result.toString());

    }

    protected void moveRight() {
            //When on click, move right
            //Update Map
            String result = mapView.moveRobotRight();
            if (mapMode=="Auto") updateMap();

            //send the command to the Raspberry pi
            sendToRPi(sendTurnRight);

            //Set message
            result = "Sending: " + sendTurnRight;
            //Display Toast
            //Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();//Display toast
            //Update Status
            TextView statusText = findViewById(R.id.statusText);//Find the debug text for the status
            statusText.setText("Status: " + result.toString());
    }

    protected void moveUp(){
            //When on click, move right
            //Update Map
            String result = mapView.moveRobotUp();
            if (mapMode=="Auto") updateMap();


            //send the command to the Raspberry pi
            sendToRPi(sendMoveForward);

            //Set results
            result = "Sending: " + sendMoveForward;

            //Display Toast
            //Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();//Display toast
            //Update Status
            TextView statusText = findViewById(R.id.statusText);//Find the debug text for the status
            statusText.setText("Status: " + result.toString());
    }

    protected void moveDown(){
            //When on click, move right
            //Update Map
            String result = mapView.moveRobotDown();
            if (mapMode=="Auto") updateMap();
            //send the command to the Raspberry pi
            sendToRPi(sendMoveBack);
            //Set results
            result = "Sending: " + sendMoveBack;

            //Display Toast
            //Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();//Display toast
            //Update Status
            TextView statusText = findViewById(R.id.statusText);//Find the debug text for the status
            statusText.setText("Status: " + result.toString());
    }

    protected void sendToRPi(String message){
        //send the message to the Raspberry pi
        byte[] bytes = message.getBytes(Charset.defaultCharset());
        if (connectedDevice != null) BluetoothChat.writeMsg(bytes);

    }


    //This BroadcastReceiver is for reconnecting to the robot if it disconnects
    BroadcastReceiver btConnectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.d(TAG, "Receiving btConnectionStatus Msg!!!");

            String connectionStatus = intent.getStringExtra("ConnectionStatus");
            myBluetoothConnectionDevice = intent.getParcelableExtra("Device");
            //myBTConnectionDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            //DISCONNECTED FROM BLUETOOTH CHAT
            if (connectionStatus.equals("disconnect")) {

                Log.d("MainActivity:", "Device Disconnected");

                if(connectIntent != null) {
                    //Stop Bluetooth Connection Service
                    stopService(connectIntent);
                }

                connectedDevice = null;
                connectedState = false;
                connectionStatusBox.setText(R.string.btStatusOffline);

                if (currentActivity) {

                    //RECONNECT DIALOG MSG
                    AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                    alertDialog.setTitle("BLUETOOTH DISCONNECTED");
                    alertDialog.setMessage("Connection with device: '" + myBluetoothConnectionDevice.getName() + "' has ended. Do you want to reconnect?");
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                    //START BT CONNECTION SERVICE
                                    Intent connectIntent = new Intent(MainActivity.this, BluetoothConnectionService.class);
                                    connectIntent.putExtra("serviceType", "connect");
                                    connectIntent.putExtra("device", myBluetoothConnectionDevice);
                                    connectIntent.putExtra("id", myUUID);
                                    startService(connectIntent);
                                }
                            });
                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    connectIntent = new Intent(MainActivity.this, BluetoothConnectionService.class);
                                    connectIntent.putExtra("serviceType", "listen");
                                    //connectIntent.putExtra("device", device);
                                    //connectIntent.putExtra("id", uuid);
                                    startService(connectIntent);
                                }
                            });
                    alertDialog.show();

                }
            }

            //SUCCESSFULLY CONNECTED TO BLUETOOTH DEVICE
            else if (connectionStatus.equals("connect")) {

                connectedDevice = myBluetoothConnectionDevice.getName();
                connectedState = true;
                Log.d("MainActivity:", "Device Connected " + connectedState);
                connectionStatusBox.setText(connectedDevice);
                Toast.makeText(MainActivity.this, "Connection Established: " + myBluetoothConnectionDevice.getName(),
                        Toast.LENGTH_LONG).show();
            }

            //BLUETOOTH CONNECTION FAILED
            else if (connectionStatus.equals("connectionFail")) {
                Toast.makeText(MainActivity.this, "Connection Failed: " + myBluetoothConnectionDevice.getName(),
                        Toast.LENGTH_LONG).show();
            }

        }
    };

    /*
       ONCLICKLISTENER FOR TILT SWITCH
   */
    public void onClickTiltSwitch() {

        tiltSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {

                    tiltNavi = true;
                    Toast.makeText(MainActivity.this, "Tilt Switch On!!", Toast.LENGTH_SHORT).show();

                } else {

                    tiltNavi = false;
                    Toast.makeText(MainActivity.this, "Tilt Switch Off!!", Toast.LENGTH_SHORT).show();


                }
            }
        });


    }

    //METHOD FOR TILT SENSING (NAVIGATION)
    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];

        //CHECK IF TILT SWITCH IS ENABLED
        if (tiltNavi) {

            //CHECK IF CONNECTED TO DEVICE FIRST
            if (connectedDevice == null) {
                Toast.makeText(MainActivity.this, "Please Connect to a Device First!!",
                        Toast.LENGTH_SHORT).show();
            } else {

                if (Math.abs(x) > Math.abs(y)) {
                    if (x < 0) {
                        Log.d("MainActivity:", "RIGHT TILT!!");

                       sendToRPi(sendTurnRight);
                       /* Toast.makeText(MainActivity.this, "Right Movement Detected!!",
                                Toast.LENGTH_SHORT).show();*/
                    }
                    if (x > 0) {
                        Log.d("MainActivity:", "LEFT TILT!!");

                        sendToRPi(sendTurnLeft);
                        /*Toast.makeText(MainActivity.this, "Left Movement Detected!!",
                                Toast.LENGTH_SHORT).show();*/
                    }
                } else {
                    if (y < 0) {
                        Log.d("MainActivity:", "UP TILT!!");

                        sendToRPi(sendMoveForward);
                        /*Toast.makeText(MainActivity.this, "Forward Movement Detected!!",
                                Toast.LENGTH_SHORT).show();*/
                    }
                    if (y > 0) {
                        Log.d("MainActivity:", "DOWN TILT!!");

                        sendToRPi(sendMoveBack);
                        /*Toast.makeText(MainActivity.this, "Down Movement Detected!!",
                                Toast.LENGTH_SHORT).show();*/
                    }
                }
       /* if (x > (-2) && x < (2) && y > (-2) && y < (2)) {
            Log.d("MainActivity:", "NOT TILTED!!");

        }*/
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


    //BROADCAST RECEIVER FOR INCOMING MESSAGE
    BroadcastReceiver incomingMsgReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String msg = intent.getStringExtra("receivingMsg");
            //incomingText.setText(msg);

            Log.d(TAG, "Receiving incomingMsg!!!" + msg);

            //FILTER EMPTY AND CONCATENATED STRING FROM RECEIVING CHANNEL
            //if (msg.length() > 7 && msg.length() < 345) {

            //CHECK IS STARTING STRING FOR ANDROID
            //if (msg.substring(0, 7).equals("Android")) {



            String[] filteredMsg = delimiterMsg(msg.replaceAll("\\n", "").trim(), "\\|");

            int i;
            //Loop for the split messages
            for (i=0; i<filteredMsg.length;i++){
                Log.d(TAG, "filteredmsg["+i+"] :" + filteredMsg[i]);
            }

            switch (filteredMsg[0]) {
                case "map":
                    String[] parsedExplore = delimiterMsg(filteredMsg[1].replaceAll("\\n", "").trim(), " ");

                    String explored_String = parsedExplore[0];
                    String obstacle_String = parsedExplore[1];

                    String purpose = "map";

                    mdfView.setText(parsedExplore[0]);
                    obstacleView.setText(parsedExplore[1]);

//                    Toast.makeText(MainActivity.this, "CASE MAP",
//                            Toast.LENGTH_LONG).show();

                    try {
                        //ENSURE ROBOT COORDINATES IS WITHIN RANGE
                        mapView.updateMaze(purpose, explored_String, obstacle_String, "");

                        if(mapMode == "Auto"){
                            updateMap();
                        }



                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                    //If the map data contains the bot position
                    if (filteredMsg[1].contains("bot_pos")){
                        purpose = "pos";

                        String[] filteredPos = delimiterMsg(filteredMsg[2 ].replaceAll(" ","").replaceAll("\\n", "").trim(), ",");

                        Log.d(TAG, "filteredPos[0] "+filteredPos[0]);
                        Log.d(TAG, "filteredPos[1] "+filteredPos[1]);
                        Log.d(TAG, "filteredPos[2] "+filteredPos[2]);


                        try {
                            //ENSURE ROBOT COORDINATES IS WITHIN RANGE
                            mapView.updateMaze(purpose, filteredPos[1], filteredPos[0], filteredPos[2]);

                            if(mapMode == "Auto"){
                                updateMap();
                            }



                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                    break;


                case "bot_pos":
                    purpose = "pos";

                    String[] filteredPos = delimiterMsg(filteredMsg[1].replaceAll(" ","").replaceAll("\\n", "").trim(), ",");

                    Log.d(TAG, "filteredPos[0] "+filteredPos[0]);
                    Log.d(TAG, "filteredPos[1] "+filteredPos[1]);
                    Log.d(TAG, "filteredPos[2] "+filteredPos[2]);


                    try {
                        //ENSURE ROBOT COORDINATES IS WITHIN RANGE
                        mapView.updateMaze(purpose, filteredPos[1], filteredPos[0], filteredPos[2]);

                        if(mapMode == "Auto"){
                            updateMap();
                        }



                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                    break;


                case "img_pos":
                    purpose = "img";
                    String[] filteredImg = delimiterMsg(filteredMsg[1].replaceAll(" ","").replaceAll("\\n", "").trim(), ",");

                    Log.d(TAG, "filteredImg[0] "+filteredImg[0]);
                    Log.d(TAG, "filteredImg[1] "+filteredImg[1]);
                    Log.d(TAG, "filteredImg[2] "+filteredImg[2]);

                    try {
                        //ENSURE ROBOT COORDINATES IS WITHIN RANGE
                        mapView.updateMaze(purpose, filteredImg[0], filteredImg[1], filteredImg[2]);

                        if(mapMode == "Auto"){
                            updateMap();
                        }



                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                    break;
//                case "au":
//
//                    String[] mazeInfo = delimiterMsg(filteredMsg[2], ";");
//                    String purpose = filteredMsg[1];
//
//                    try {
//                        //ENSURE ROBOT COORDINATES IS WITHIN RANGE
//                        if (Integer.parseInt(mazeInfo[2]) > 0 && Integer.parseInt(mazeInfo[2]) < 14 && Integer.parseInt(mazeInfo[3]) > 0 && Integer.parseInt(mazeInfo[3]) < 19) {
//
//                            mapView.updateMaze(purpose, mazeInfo);
//
//                        }
//                        //SET ROBOT STATUS TO STOP FOR EXPLORATION WHEN ROBOT RETURN TO ORIGINAL POSITION
//                        if (mazeInfo[2].equals("13") && mazeInfo[3].equals("18") && robotStatus.getText().equals(getString(R.string.FastestPath))) {
//                            robotStatus.setText(R.string.Robot_Idle);
//                        }
//                    }
//                    catch (Exception e){
//                        e.printStackTrace();
//                    }
//                    break;
//
//                case "rs":
//
//
//                    try {
//                        if (filteredMsg[1].equals("stop")) {
//                            robotStatus.setText(R.string.Robot_Idle);
//                        } else if (filteredMsg[1].equals("moving")) {
//                            robotStatus.setText(R.string.Robot_Moving);
//                        } else if (filteredMsg[1].equals("left")) {
//                            robotStatus.setText("Turn Left");
//                        } else if (filteredMsg[1].equals("right")) {
//                            robotStatus.setText("Turn Right");
//                        }
//                    }
//                    catch (Exception e){
//                        e.printStackTrace();
//                    }
//                    break;
//
//                case "done":
//
//                    try {
//                        String[] mdfStrings = myMaze.getMDFStrings();
//                        String exploredOrNot = mdfStrings[0];
//                        String obstacleOrNot = mdfStrings[1];
//
//
//                        md5ExplorationText.setText(exploredOrNot);
//                        md5ExplorationText.setText(exploredOrNot);
//
//                        md5ObstacleText.setText(obstacleOrNot);
//
//                        String displayNumberIds = myMaze.getAllNumberIds();
//                        displayNumberIds = displayNumberIds.substring(0, displayNumberIds.length()-1);
//                        incomingText.setText(displayNumberIds);
//
//                        robotStatus.setText(R.string.Robot_Idle);
//                    }
//                    catch (Exception e){
//                        e.printStackTrace();
//                    }
//                    break;
//
//                case "id":
//
//                    try {
//                        String idAndLocation = filteredMsg[1];
//                        myMaze.addNewNumberId(idAndLocation);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
            }


        }
    };

    private String[] delimiterMsg(String msg, String delimiter) {

        return (msg.toLowerCase()).split(delimiter);
    }
}

