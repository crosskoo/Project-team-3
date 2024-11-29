package com.jeyun.rhdms;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.jeyun.rhdms.databinding.ActivityMainBinding;
import com.jeyun.rhdms.handler.SharedPreferenceHandler;
import com.jeyun.rhdms.handler.entity.User;
import com.jeyun.rhdms.handler.UserHandler;
import com.jeyun.rhdms.util.factory.AlertFactory;
import com.jeyun.rhdms.util.factory.PopupFactory;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding; // 바인딩할 뷰 레이아웃
    private PopupFactory<AlertDialog> factory; // 알림 메시지 factory
    private AlarmManager alarmManager;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private String id = "";
    private String password = "";
    private String encryptedPassword;

    private boolean isRequested = false; // 권한 요청 여부

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // 뷰 바인딩
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        factory = new AlertFactory(view.getContext());
        checkPermission(); // 권한 승인 여부 확인

        initEvent();

        // sharedPreference에서 orgnztId 불러오기
        SharedPreferenceHandler handler = new SharedPreferenceHandler(this);
        String savedOrgnztId = handler.getSavedOrgnztId();

        Log.d("ksd", "savedOrgnztId : " + savedOrgnztId); // 테스트 용

        if (savedOrgnztId != null) // 값이 있다면 로그인 생략하고 바로 메뉴 화면으로 이동
        {
            User.getInstance().setOrgnztId(savedOrgnztId);
            Intent intent = new Intent(this, MenuActivity.class);
            startActivity(intent);
            finish();
        }
        // 값이 없다면 기존 절차대로 로그인 수행해야 함.
    }

    private void initEvent()
    {
        // (추후 수정) 버튼 누를 시 메인 화면으로 이동
        binding.buttonLogin.setOnClickListener(v ->
        {
            setIdPw(); // 아이디, 비밀 번호 설정

            if (id.isEmpty() || password.isEmpty()) // 아이디, 비밀 번호 둘 중 하나라도 입력하지 않으면
            {
                // 입력하라는 팝업 메시지 출력
                Toast.makeText(this, "아이디와 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            Optional<String> orgnztId = findUser(id, encryptedPassword); // id, 비밀번호에 해당하는 유저의 orgnztId를 반환.
            if (orgnztId.isPresent())
            {
                // 로그인 성공
                Log.d("assert login", "로그인 성공"); // 테스트 용
                Toast.makeText(this, "환영합니다.", Toast.LENGTH_SHORT).show();

                SharedPreferenceHandler handler = new SharedPreferenceHandler(this);
                // 임시 id 저장
                //handler.saveOrgnztId(orgnztId.get()); // orgnztId 저장;
                handler.saveOrgnztId("002"); //임시 아이디 저장

                User.getInstance().setOrgnztId(orgnztId.get()); // orgnztId 저장하는 싱글톤 클래스 생성

                Log.d("check orgnztId", User.getInstance().getOrgnztId()); // 테스트 용

                // 메인 화면으로 이동
                Intent intent_switch = new Intent(this, MenuActivity.class);
                startActivity(intent_switch);
                finish();
            }
            else
            {
                // 로그인 실패
                Log.d("ksd", "로그인 실패");
                Toast.makeText(this, "로그인에 실패했습니다. 아이디 혹은 비밀번호를 정확히 입력해주세요.", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override // 권한 요청 결과 처리
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        checkPermission();
    }

    private void checkPermission() // 권환 승인 확인 함수(블루투스 권한, 알림 권한 등)
    {
        // 안드로이드 12 (API 31) 이상에서 블루투스 권한 승인 여부
        boolean isBluetoothPermissionDenied = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED;

        // 안드로이드 13 (API 33) 이상에서 알람 권한 승인 여부
        boolean isNotificationPermissionDenied = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED;

        // 진동 권한 승인 여부
        boolean isVibratePermissionDenied = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.VIBRATE) != PackageManager.PERMISSION_GRANTED;

//         안드로이드 12 (API 31) 이상에서 정확한 알람 권한 승인 여부
         alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
         boolean isScheduleExactAlarmDenied = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms();

        // 여러 권한 중 하나라도 승인이 안 되어 있다면
        if (isBluetoothPermissionDenied || isNotificationPermissionDenied || isVibratePermissionDenied)
        {
            AlertDialog alert;
            if (isRequested) // 권한 요청은 전에 했지만 권한 승인이 되지 않은 경우
            {
                alert = factory.createPopup("권한 요청", "수동으로 권한을 활성화해주세요!", (dialog, which) -> gotoAppSettings(Settings.ACTION_APPLICATION_DETAILS_SETTINGS));
            }
            else // 처음 권한 요청을 하는 경우
            {
                alert = factory.createPopupYN("권한 요청", "해당 권한이 필수적으로 요구됩니다.",
                        (dialog, which) -> requestPermission(),
                        (dialog, which) -> gotoAppSettings(Settings.ACTION_APPLICATION_DETAILS_SETTINGS));
            }

            alert.show();
        }
        AlertDialog alert;
        if(isScheduleExactAlarmDenied){
            alert = factory.createPopup("권한 요청", "복약 푸시알림을 위해 알람 및 리마인더 설정을 허용해주세요!", (dialog, which) -> gotoAppSettings(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM));
            alert.show();
        }
    }

    private void gotoAppSettings(String settingsAction) // App 설정으로 이동
    {
        Intent intent = new Intent(settingsAction,
                Uri.fromParts("package", getPackageName(), null));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void requestPermission()
    {
        List<String> permissionsList = new ArrayList<>(); // 요청할 권한을 저장할 리스트

        // 안드로이드 12 (API 31) 이상에서 블루투스 권한 승인이 안 되어 있다면
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED)
        {
            permissionsList.add(android.Manifest.permission.BLUETOOTH_CONNECT);
        }

        // 안드로이드 13 (API 33) 이상에서 알람 권한 승인이 안 되어 있다면
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
        {
            permissionsList.add(Manifest.permission.POST_NOTIFICATIONS);
        }

        // 진동 권한 승인이 안되어 있다면
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.VIBRATE) != PackageManager.PERMISSION_GRANTED)
        {
            permissionsList.add(Manifest.permission.VIBRATE);
        }

        // 안드로이드 12 (API 31) 이상에서 정확한 알람 권한 승인이 안 되어 있다면
        /*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                !alarmManager.canScheduleExactAlarms())
        {
            Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
            startActivity(intent);
        }
        */

        if (!permissionsList.isEmpty()) // 요청할 권한이 있다면
        {
            isRequested = true;
            String[] permissions = permissionsList.toArray(new String[0]);
            ActivityCompat.requestPermissions(this, permissions, permissions.length);
        }
    }

    private void setIdPw() // 로그인할 데이터 전처리
    {
        id = binding.inputId.getText().toString();
        password = binding.inputPw.getText().toString();
        encryptedPassword = "";

        try
        {
            encryptedPassword = encryptPassword(password, id);
            // Toast.makeText(this, "id : " + id + ", pw : " + encryptedPassword, Toast.LENGTH_SHORT).show(); // 확인용
        }
        catch (Exception e)
        {
            Toast.makeText(this, "비밀 번호를 암호화 하는데 실패했습니다.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private String encryptPassword(@NonNull String password, @NonNull String id) throws Exception // 아이디, 비밀 번호로 해시값 생성
    {
        byte[] hashValue = null;

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.reset();
        md.update(id.getBytes());

        hashValue = md.digest(password.getBytes());
        Log.d("ksd", "hashValue : " + Base64.getEncoder().encodeToString(hashValue)); // 테스트 용

        return Base64.getEncoder().encodeToString(hashValue);
    }

    private Optional<String> findUser(String id, String encryptedPassword) // DB로부터 유저 정보가 있는지 확인하는 함수
    {
        Callable<Optional<String>> task = () -> {
            UserHandler userHandler = new UserHandler();
            return userHandler.findUserInfo(id, encryptedPassword);
        };

        Future<Optional<String>> future = executor.submit(task);

        try
        {
            return future.get();
        }
        catch (InterruptedException | ExecutionException e)
        {
            e.printStackTrace();
            return Optional.empty();
        }
    }
}