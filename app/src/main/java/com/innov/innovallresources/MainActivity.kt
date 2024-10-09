package com.innov.innovallresources

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.innov.innovallresources.ui.theme.InnovAllResourcesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InnovAllResourcesTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "GeoTracking",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }


    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Start $name!",
        modifier = modifier
    )


}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    InnovAllResourcesTheme {
        Greeting("Trip Tracking")
    }
}



@Composable
fun ClickableButton() {
    // State to hold the click count
    val clickCount = remember { mutableStateOf(0) }
    val context = LocalContext.current
    Button(onClick = {
        // Increment click count on button click


      /*  val intent = Intent(context, GeoTrackingActivity::class.java)
        context.startActivity(intent)*/
    }) {
        Text(text = "Start Trip")

    }
}

@Preview
@Composable
fun PreviewClickableButton() {
    ClickableButton()
}
