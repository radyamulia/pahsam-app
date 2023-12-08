package com.example.pahsamapp.ui.activities.main

import android.Manifest
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pahsamapp.R
import com.example.pahsamapp.data.FirestoreManager
import com.example.pahsamapp.ui.theme.PahsamAppTheme
import android.util.Log
import android.widget.TextView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.pahsamapp.ui.activities.success.SuccessActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnSuccessListener
import kotlinx.coroutines.delay
import java.io.IOException
import java.util.Locale


class MainActivity : ComponentActivity() {
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var latitude: String = ""
    private var longitude: String = ""
    var address: String = ""
//    private var address {mutableStateOf("")}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PahsamAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    fusedLocationProviderClient =
                        LocationServices.getFusedLocationProviderClient(this)
                    MainScreen()
                }
            }
        }
    }

    private fun getLastLocation() {
//        var addressToReturn: String = ""
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationProviderClient?.lastLocation?.addOnSuccessListener(
                this,
                OnSuccessListener { location ->
                    if (location != null) {
                        try {
                            val geocoder = Geocoder(this, Locale.getDefault())
                            val addresses = geocoder.getFromLocation(
                                location.latitude,
                                location.longitude,
                                1
                            )
                            address =  (addresses?.get(0)?.getAddressLine(0)
                                ?: "Lokasi Tidak Ditemukan!")
                            Log.d("Atas", address)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                })
        } else {
            askPermission()
        }
        Log.d("Bawah", address)
    }

    companion object {
        public const val REQUEST_CODE = 100
    }

    private fun askPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_CODE
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainScreen() {
        val stateAddress = remember {mutableStateOf("")}
        var stringAddress: String = ""
        val activity = LocalContext.current as Activity
        LaunchedEffect(address){
            delay(3000)
            stateAddress.value = address
            stringAddress = address
        }
        var showDialogPenuh by remember { mutableStateOf(false) }
    var showDialogHampir by remember { mutableStateOf(false) }

    // Dialog tempat sampah penuh
    if (showDialogPenuh) {
        AlertDialog(
            containerColor = Color.White,
            onDismissRequest = { showDialogPenuh = false },
            title = {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Sampah Telah Penuh?",
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            color = Color.Red,
                            )
                    }
                    },
            text = {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text ="Apakah Anda yakin ingin melaporkan sampah yang telah penuh?",
                        textAlign = TextAlign.Center
                        )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        FirestoreManager.addListData(
                            address = stateAddress.value.toString(),
                            status = "Sampah Telah Penuh",
                            onSuccess = {
                                Log.d(TAG, "Data added successfully")
                                val intent = Intent(activity, SuccessActivity::class.java)
                                activity.startActivity(intent)
                            },
                            onFailure = { e ->
                                Log.w(TAG, "Error adding data: $e")
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(colorResource(R.color.primary))
                ) {
                    Text("Yakin dan Kirim Laporan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialogPenuh = false }) {
                    Text(
                        text = "Batalkan",
                        color = Color.Red
                    )
                }
            },
        )
    }

    // Dialog tempat sampah hampir penuh
    if (showDialogHampir) {
        AlertDialog(
            containerColor = Color.White,
            onDismissRequest = { showDialogHampir = false },
            title = {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Sampah Hampir Penuh?",
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(R.color.warning),
                    )
                }
            },
            text = {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text ="Apakah Anda yakin ingin melaporkan sampah yang hampir penuh?",
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        FirestoreManager.addListData(
                            address = stateAddress.value.toString(),
                            status = "Sampah Hampir Penuh",
                            onSuccess = {
                                Log.d(TAG, "Data added successfully")
                                val intent = Intent(activity, SuccessActivity::class.java)
                                activity.startActivity(intent)
                            },
                            onFailure = { e ->
                                Log.w(TAG, "Error adding data: $e")
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(colorResource(R.color.primary))
                ) {
                    Text("Yakin dan Kirim Laporan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialogHampir = false }) {
                    Text(
                        text = "Batalkan",
                        color = Color.Red
                    )
                }
            },
        )
    }
    Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column {
                Image(
                    painter = painterResource(R.drawable.mockmap),
                    contentDescription = "mock google map",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxHeight()
                )
            }
            CenterAlignedTopAppBar(
                title = {
                    Image(
                        painter = painterResource(R.drawable.logo_green),
                        contentDescription = "Logo Pahsam",
                        modifier = Modifier
                            .height(40.dp)
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                )
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color.White)
                    .align(alignment = Alignment.BottomStart)
                    .padding(30.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row {
                        Image(
                            painter = painterResource(R.drawable.location),
                            contentDescription = "location",
                            modifier = Modifier.height(25.dp)
                        )
                        Spacer(modifier = Modifier.width(15.dp))
                        Box (
                            modifier = Modifier
                                .widthIn(5.dp, 200.dp)
                        ) {
                            Text(
                                text = stateAddress.value,
                            )
                        }
                    }
                    Image(
                        painter = painterResource(R.drawable.edit_location),
                        contentDescription = "edit location",
                        modifier = Modifier
                            .height(25.dp)
                            .clickable {
                                getLastLocation()
                                stateAddress.value = address
                                stringAddress = address
                            }
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        showDialogHampir = showDialogHampir.not()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.warning)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Sampah Hampir Penuh",
                        color = Color.Black
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = { showDialogPenuh = showDialogPenuh.not() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Sampah Telah Penuh"
                    )
                }
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun Preview() {
        PahsamAppTheme {
            MainScreen()
        }
    }
}