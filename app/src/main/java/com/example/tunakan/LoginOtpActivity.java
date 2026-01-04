package com.example.tunakan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.tunakan.utils.AndroidUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthSettings;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class LoginOtpActivity extends AppCompatActivity {

    private static final String TAG = "LoginOtpActivity";

    // Test phone number - MUST be added in Firebase Console:
    // Firebase Console -> Authentication -> Sign-in method -> Phone -> Phone numbers for testing
    // Add: +40799999999 with code 123456
    private static final String TEST_PHONE_NUMBER = "+40799999999";
    private static final String TEST_VERIFICATION_CODE = "123456";

    // Set to false - Firebase Auth Emulator requires running emulator suite on your computer
    private static final boolean USE_EMULATOR = false;

    String phoneNumber;
    Long timeoutSeconds = 60L;
    String verificationCode;
    PhoneAuthProvider.ForceResendingToken resendingToken;

    EditText otpInput;
    Button nextBtn;
    ProgressBar progressBar;
    TextView resendOtpTextView;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_otp);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Use emulator for local testing (bypasses all SHA-1 and phone verification issues)
        if (USE_EMULATOR) {
            try {
                mAuth.useEmulator("10.0.2.2", 9099);
                Log.d(TAG, "Using Firebase Auth Emulator");
            } catch (Exception e) {
                Log.e(TAG, "Emulator already configured or error: " + e.getMessage());
            }
        }

        otpInput = findViewById(R.id.login_otp);
        nextBtn = findViewById(R.id.login_next_btn);
        progressBar = findViewById(R.id.login_progress_bar);
        resendOtpTextView = findViewById(R.id.resend_otp_textview);

        phoneNumber = getIntent().getExtras().getString("phone");

        Log.d(TAG, "Phone number received: " + phoneNumber);
        Log.d(TAG, "Test phone number: " + TEST_PHONE_NUMBER);
        Log.d(TAG, "Phone matches test number: " + phoneNumber.equals(TEST_PHONE_NUMBER));

        if (phoneNumber.equals(TEST_PHONE_NUMBER)) {
            Log.d(TAG, "USING TEST PHONE NUMBER - Should auto-verify if configured in Firebase Console");
        }

        // Setup test phone auth for emulator testing
        // This MUST be called before sendOtp()
        setupTestPhoneAuth();

        sendOtp(phoneNumber, false);

        nextBtn.setOnClickListener(v -> {
            String enteredOtp = otpInput.getText().toString();

            if (enteredOtp.isEmpty() || enteredOtp.length() < 6) {
                otpInput.setError("Please enter a valid 6-digit OTP");
                return;
            }

            if (verificationCode == null) {
                AndroidUtil.showToast(getApplicationContext(), "Please wait for OTP to be sent");
                return;
            }

            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationCode, enteredOtp);
            signIn(credential);
        });

        resendOtpTextView.setOnClickListener((v) -> {
            sendOtp(phoneNumber, true);
        });
    }

    /**
     * Setup test phone authentication for development/emulator testing.
     * This allows automatic verification for the test phone number without real SMS.
     *
     * IMPORTANT: You MUST also add this test number in Firebase Console:
     * Firebase Console -> Authentication -> Sign-in method -> Phone -> Phone numbers for testing
     * Add: +40799999999 with code 123456
     *
     * Then in the app, use:
     * - Country: Romania (+40)
     * - Phone: 799999999
     * - OTP: 123456
     */
    void setupTestPhoneAuth() {
        // COMMENTED OUT: This auto-verifies without showing OTP screen
        // Uncomment if you want auto-verification (skips OTP input)
        /*
        try {
            FirebaseAuthSettings firebaseAuthSettings = mAuth.getFirebaseAuthSettings();
            firebaseAuthSettings.setAutoRetrievedSmsCodeForPhoneNumber(TEST_PHONE_NUMBER, TEST_VERIFICATION_CODE);
            Log.d(TAG, "TEST PHONE AUTH ENABLED - Auto verification ON");
        } catch (Exception e) {
            Log.e(TAG, "Failed to setup test phone auth: " + e.getMessage());
        }
        */

        Log.d(TAG, "Test phone auth: Manual OTP entry required");
        Log.d(TAG, "Use phone: " + TEST_PHONE_NUMBER + " with code: " + TEST_VERIFICATION_CODE);
    }

    void sendOtp(String phoneNumber, boolean isResend) {
        startResendTimer();
        setInProgress(true);

        Log.d(TAG, "Sending OTP to: " + phoneNumber + ", isResend: " + isResend);

        PhoneAuthOptions.Builder builder =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(timeoutSeconds, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                                Log.d(TAG, "onVerificationCompleted: Auto verification triggered");
                                signIn(phoneAuthCredential);
                                setInProgress(false);
                            }

                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {
                                Log.e(TAG, "===========================================");
                                Log.e(TAG, "onVerificationFailed!");
                                Log.e(TAG, "Error class: " + e.getClass().getName());
                                Log.e(TAG, "Error message: " + e.getMessage());
                                Log.e(TAG, "Phone number used: " + phoneNumber);

                                // Check for FirebaseAuthException to get error code
                                if (e instanceof com.google.firebase.auth.FirebaseAuthException) {
                                    com.google.firebase.auth.FirebaseAuthException authEx = (com.google.firebase.auth.FirebaseAuthException) e;
                                    Log.e(TAG, "Firebase Auth Error Code: " + authEx.getErrorCode());
                                }

                                Log.e(TAG, "Full exception: ", e);
                                Log.e(TAG, "===========================================");

                                // Show the ACTUAL error message from Firebase
                                String actualError = e.getMessage() != null ? e.getMessage() : "Unknown error";
                                AndroidUtil.showToast(getApplicationContext(), "Firebase Error: " + actualError);
                                setInProgress(false);
                            }

                            @Override
                            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                super.onCodeSent(s, forceResendingToken);
                                Log.d(TAG, "onCodeSent: OTP sent successfully");
                                verificationCode = s;
                                resendingToken = forceResendingToken;
                                AndroidUtil.showToast(getApplicationContext(), "OTP sent successfully");
                                setInProgress(false);
                            }
                        });

        if (isResend && resendingToken != null) {
            PhoneAuthProvider.verifyPhoneNumber(builder.setForceResendingToken(resendingToken).build());
        } else {
            PhoneAuthProvider.verifyPhoneNumber(builder.build());
        }
    }

    void setInProgress(boolean inProgress){
        if(inProgress){
            progressBar.setVisibility(View.VISIBLE);
            nextBtn.setVisibility(View.GONE);
        }else{
            progressBar.setVisibility(View.GONE);
            nextBtn.setVisibility(View.VISIBLE);
        }
    }

    void signIn(PhoneAuthCredential phoneAuthCredential){
        //login and go to next activity
        setInProgress(true);
        mAuth.signInWithCredential(phoneAuthCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                setInProgress(false);
                if(task.isSuccessful()){
                    Intent intent = new Intent(LoginOtpActivity.this,LoginUsernameActivity.class);
                    intent.putExtra("phone",phoneNumber);
                    startActivity(intent);
                }else{
                    // Log the actual sign-in error
                    Log.e(TAG, "signIn failed: " + task.getException().getMessage(), task.getException());
                    AndroidUtil.showToast(getApplicationContext(),"OTP verification failed: " + task.getException().getMessage());
                }
            }
        });


    }

    void startResendTimer(){
        resendOtpTextView.setEnabled(false);
        final long[] remaining = {60L};
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                remaining[0]--;
                runOnUiThread(() -> {
                    resendOtpTextView.setText("Resend OTP in " + remaining[0] + " seconds");
                    if(remaining[0] <= 0){
                        timer.cancel();
                        resendOtpTextView.setEnabled(true);
                        resendOtpTextView.setText("Resend OTP");
                    }
                });
            }
        }, 1000, 1000);
    }


}
