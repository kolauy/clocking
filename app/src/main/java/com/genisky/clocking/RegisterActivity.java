package com.genisky.clocking;

import android.accounts.AccountManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.genisky.account.AuthenticationRequest;
import com.genisky.account.AuthenticationResponse;
import com.genisky.account.DatabaseManager;
import com.genisky.server.GeniskyAuthenticate;
import com.genisky.server.GeniskyServices;

import java.security.MessageDigest;

public class RegisterActivity extends AppCompatActivity {
    private UserLoginTask _task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        DatabaseManager manager = new DatabaseManager(getApplicationContext());
        if (manager.isAccountExist())
        {
            AuthenticationRequest request = manager.getAuthenticationRequest();
            String imei = ((TelephonyManager)getSystemService(TELEPHONY_SERVICE)).getDeviceId();
            if (imei.equals(request.imei))
            {
                ((EditText)findViewById(R.id.phone)).setText(request.phone);
                ((EditText)findViewById(R.id.password)).setText(request.password);
            }
        }
        manager.close();

        findViewById(R.id.register_submit_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
            }
        });
    }

    private void submit() {
        if (_task != null) {
            return;
        }
        EditText phone = (EditText)findViewById(R.id.phone);
        EditText password = (EditText)findViewById(R.id.password);
        phone.setError(null);
        password.setError(null);

        if (!isPasswordValid(password.getText().toString())) {
            password.setError("密码必须大于6个字符");
            password.requestFocus();
            return;
        }
        if (!isPhoneValid(phone.getText().toString())) {
            phone.setError("非法的电话号码");
            phone.requestFocus();
            return;
        }
        _task = new UserLoginTask(phone.getText().toString(), password.getText().toString());
        _task.execute();
    }

    private boolean isPhoneValid(String phone) {
        return phone.length() == 11;
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 6;
    }

    private class UserLoginTask extends AsyncTask<Void, Void, Intent> {

        private final String _password;
        private final GeniskyAuthenticate _authenticate;
        private AuthenticationRequest _request;
        private AuthenticationResponse _response;

        UserLoginTask(String phone, String password) {
            _password = password;
            _request = new AuthenticationRequest();
            _request.imei = ((TelephonyManager)getSystemService(TELEPHONY_SERVICE)).getDeviceId();
            _request.phone = phone;
            _request.password = MD5(password);
            _authenticate = new GeniskyAuthenticate(getString(R.string.authenticationService));
        }

        @Override
        protected Intent doInBackground(Void... params) {
            Bundle data = new Bundle();
            try{
                _response = _authenticate.register(_request);
                if (_response == null){
                    throw new Exception("用户登陆失败");
                }
                GeniskyServices services = new GeniskyServices(_response);
                String state = services.getUserState();
                if (state == null || !state.equals("active")){
                    throw new Exception("无效用户");
                }
            } catch (Exception e) {
                data.putString(AccountManager.KEY_ERROR_MESSAGE, e.getMessage());
            }
            final Intent result = new Intent();
            result.putExtras(data);
            return result;
        }

        @Override
        protected void onPostExecute(final Intent intent) {
            try{
                _task = null;

                if (intent.hasExtra(AccountManager.KEY_ERROR_MESSAGE)) {
                    throw new Exception(intent.getStringExtra(AccountManager.KEY_ERROR_MESSAGE));
                }
                try(DatabaseManager manager = new DatabaseManager(getApplicationContext())){
                    _request.password = _password;
                    manager.saveAccount(_request, _response);
                }
                setResult(RESULT_OK, intent);
                finish();
            }
            catch (Exception e){
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            _task = null;
        }

        public String MD5(String password) {
            char hexDigits[]={'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};

            try {
                byte[] btInput = password.getBytes();
                MessageDigest mdInst = MessageDigest.getInstance("MD5");
                mdInst.update(btInput);
                byte[] md = mdInst.digest();
                char str[] = new char[md.length * 2];
                int k = 0;
                for (byte byte0 : md) {
                    str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                    str[k++] = hexDigits[byte0 & 0xf];
                }
                return new String(str);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
