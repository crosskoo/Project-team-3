package com.jeyun.rhdms;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.jeyun.rhdms.databinding.ActivityMainBinding;
import com.jeyun.rhdms.util.factory.AlertFactory;
import com.jeyun.rhdms.util.factory.PopupFactory;

import org.sql2o.Sql2o;

import java.security.MessageDigest;
import java.security.spec.ECField;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding; // 바인딩할 뷰 레이아웃
    private PopupFactory<AlertDialog> factory; // 알림 메시지 factory

    private boolean isRequested = false; // 권한 요청 여부

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // 뷰 바인딩
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        factory = new AlertFactory(view.getContext());
        checkPermission(); // 권한 승인 여부 확인

        initEvent();
    }

    private void initEvent()
    {
        // (추후 수정) 버튼 누를 시 메인 화면으로 이동
        binding.buttonLogin.setOnClickListener(v ->
        {
            /*
            Intent intent_switch = new Intent(getApplicationContext(), MenuActivity.class);
            startActivity(intent_switch);
            finish();

             */
            Login();
        });
    }

    @Override // 권한 요청 결과 처리
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        checkPermission();
    }

    private void checkPermission() // 권환 승인 확인 함수(블루투스 권한, 알림 권한)
    {
        // 안드로이드 12 (API 31) 이상에서 블루투스 권한 승인 여부
        boolean isBluetoothPermissionDenied = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED;

        // 안드로이드 13 (API 33) 이상에서 알람 권한 승인 여부
        boolean isNotificationPermissionDenied = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED;

        // 두 권한 중 하나라도 승인이 안 되어 있다면
        if (isBluetoothPermissionDenied || isNotificationPermissionDenied)
        {
            AlertDialog alert;
            if (isRequested) // 권한 요청은 전에 했지만 권한 승인이 되지 않은 경우
            {
                alert = factory.createPopup("권한 요청", "수동으로 권한을 활성화해주세요!", (dialog, which) -> gotoAppSettings());
            }
            else // 처음 권한 요청을 하는 경우
            {
                alert = factory.createPopupYN("권한 요청", "해당 권한이 필수적으로 요구됩니다.",
                        (dialog, which) -> requestPermission(),
                        (dialog, which) -> gotoAppSettings());
            }

            alert.show();
        }
    }

    private void gotoAppSettings() // App 설정으로 이동
    {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", getPackageName(), null));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
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

        if (!permissionsList.isEmpty()) // 요청할 권한이 있다면
        {
            isRequested = true;
            String[] permissions = permissionsList.toArray(new String[0]);
            ActivityCompat.requestPermissions(this, permissions, permissions.length);
        }
    }

    private void Login() // 로그인할 데이터 전처리
    {
        String id = binding.inputId.getText().toString();
        String password = binding.inputPw.getText().toString();

        if (id.isEmpty() || password.isEmpty())
        {
            Toast.makeText(this, "아이디와 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        try
        {
            String encryptedPassword = encryptPassword(password, id);
            Toast.makeText(this, "id : " + id + ", pw : " + encryptedPassword, Toast.LENGTH_SHORT).show(); // 확인용
        }
        catch (Exception e)
        {
            Toast.makeText(this, "로그인 하는 데 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
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

        return Base64.getEncoder().encodeToString(hashValue);
    }

    /*
    private void getUserInfo()
    {
        String url = "jdbc:jtds:sqlserver://211.229.106.53:11433/사용성평가";
        String username = "sa";
        String userpassword = "test1q2w3e@@";

        Sql2o client = new Sql2o(url, username, userpassword);
    }

     */
}