package com.rodrigoguerra.firebasecurso;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{

    private static final String TAG = "MainActivity" ;
    private static final int SIGN_IN_GOOGLE_CODE = 1;//es por la cantidad de intents esta tendra el numero 1
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private GoogleApiClient googleApiClient;

    private Button btnCreateAccount;
    private Button btnSignIn;
    private SignInButton btnSignInGoogle;

    private EditText edtEmail;
    private EditText edtPassWord;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnCreateAccount = (Button) findViewById(R.id.btnCreateAccount);
        btnSignIn        = (Button) findViewById(R.id.btnSignIn);
        btnSignInGoogle = (SignInButton) findViewById(R.id.btnSignInButtonGoogle);

        edtEmail         = (EditText) findViewById(R.id.edtemail);
        edtPassWord      = (EditText) findViewById(R.id.edtpassword);

        initialize();//va a contener toda la inicializacion de los objetos de arriba

        btnCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAccount(edtEmail.getText().toString(), edtPassWord.getText().toString());
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn(edtEmail.getText().toString(), edtPassWord.getText().toString());
            }
        });

        btnSignInGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                startActivityForResult(intent, SIGN_IN_GOOGLE_CODE);
                //el SIGN_IN_GOOGLE_CODE es una bandera que nosotros creamos para los intents
            }
        });



    }

    private void initialize(){

        firebaseAuth  = FirebaseAuth.getInstance();//instacia el objeto rellenalo.
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();//aqu detecta que hubo un cambio en la sesion. cuando detecte algo en la sesion haga algo
                if (firebaseUser != null){ //el objeto si existe y podemos ejecutar algo
                    Log.w(TAG,"onAuthStateChanged - signed_in " + firebaseUser.getUid());
                    Log.w(TAG,"onAuthStateChanged - signed_in " + firebaseUser.getEmail());
                    Intent i = new Intent(MainActivity.this, WelcomeActivity.class);
                    startActivity(i);
                    finish();

                }else {
                    Log.w(TAG,"onAuthStateChanged - signed_out");
                }
            }
        };

        //Inicializacion de Google Account
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();


    }
//el sign in será con correo y contraseña
    private void signIn(String email, String password){
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    Toast.makeText(MainActivity.this, "Authentication Success", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(MainActivity.this, WelcomeActivity.class);
                    startActivity(i);
                    finish();// elimina la actividad anterior
                } else {
                    Toast.makeText(MainActivity.this, "Authentication Unsuccess", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private  void  createAccount(String email, String password){ //se agrega un listener para saber como fue la creacion de cuenta
        firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    Toast.makeText(MainActivity.this, "Create Account Success", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Create Account Unsuccess", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
//el sign in sera con google
    private void signInGoogleFirebase(GoogleSignInResult googleSignInResult){
        //hasta aqui ya hicmos signin en google ahora falta añadir la cuenta en firebase
        if (googleSignInResult.isSuccess()){
            AuthCredential authCredential = GoogleAuthProvider.getCredential(googleSignInResult.getSignInAccount().getIdToken(), null);
            firebaseAuth.signInWithCredential(authCredential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(MainActivity.this, "Google Authentication Success", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(MainActivity.this, WelcomeActivity.class);
                        startActivity(i);
                        finish();// elimina la actividad anterior
                    } else {
                        Toast.makeText(MainActivity.this, "Google Authentication Unsuccess", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }else{
                Toast.makeText(MainActivity.this, "Google Sign In Unsuccess", Toast.LENGTH_SHORT).show();
            }

        }




    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseAuth.removeAuthStateListener(authStateListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //identificador de respuesta
        if (requestCode == SIGN_IN_GOOGLE_CODE){
            GoogleSignInResult googleSignInResult = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            //en esta linea es donde se hace el sign in. Esto va a dar un resultado que se maneja en otro metodo
            signInGoogleFirebase(googleSignInResult);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
//metodo incluido al impletar GoogleApiClient.OnConnectionFailedListener en la inicializacion de google
    }
}
