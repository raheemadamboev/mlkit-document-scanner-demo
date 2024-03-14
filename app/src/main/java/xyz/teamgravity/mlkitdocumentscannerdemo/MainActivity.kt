package xyz.teamgravity.mlkitdocumentscannerdemo

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import coil.compose.AsyncImage
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import xyz.teamgravity.mlkitdocumentscannerdemo.ui.theme.MLKitDocumentScannerDemoTheme
import java.io.File
import java.io.FileOutputStream

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val options = GmsDocumentScannerOptions.Builder()
            .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
            .setGalleryImportAllowed(true)
            .setPageLimit(5)
            .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG, GmsDocumentScannerOptions.RESULT_FORMAT_PDF)
            .build()
        val scanner = GmsDocumentScanning.getClient(options)

        setContent {
            MLKitDocumentScannerDemoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var images by remember { mutableStateOf(emptyList<Uri>()) }

                    val launcher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.StartIntentSenderForResult(),
                        onResult = { result ->
                            if (result.resultCode == RESULT_OK) {
                                val answer = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
                                images = answer?.pages?.map { it.imageUri } ?: emptyList()

                                answer?.pdf?.let { pdf ->
                                    val output = FileOutputStream(File(filesDir, "scan.pdf"))
                                    contentResolver.openInputStream(pdf.uri)?.use { it.copyTo(output) }
                                }
                            }
                        }
                    )

                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Button(
                            onClick = {
                                scanner.getStartScanIntent(this@MainActivity).addOnSuccessListener { sender ->
                                    launcher.launch(IntentSenderRequest.Builder(sender).build())
                                }.addOnFailureListener { exception ->
                                    Toast.makeText(applicationContext, exception.localizedMessage, Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Text(
                                text = stringResource(id = R.string.scan_pdf)
                            )
                        }

                        images.forEach { image ->
                            AsyncImage(
                                model = image,
                                contentDescription = null,
                                contentScale = ContentScale.FillWidth,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}