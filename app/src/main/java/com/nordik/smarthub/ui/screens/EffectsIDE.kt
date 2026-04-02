package com.nordik.smarthub.ui.screens

import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nordik.smarthub.R
import java.io.File

import com.nordik.smarthub.ui.InputTextDialog

@Composable
fun EffectsEdit() {
    var expanded by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var file by remember { mutableStateOf<File?>(null) }
    var dialogTitle by remember { mutableStateOf<String>("") }
    var dialogAction by remember { mutableStateOf<(String) -> Unit>({}) }
    var fileContent by remember { mutableStateOf("") }

    val createTitle = stringResource(R.string.IDE_file_create_title)
    val openTitle = stringResource(R.string.IDE_file_open_title)

    val locContext = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            val fileContent = locContext.contentResolver.openInputStream(it)
                ?.bufferedReader()
                ?.readText()
        }
    }
    Column(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
        Row(modifier = Modifier.fillMaxWidth().height(20.dp)) {
            Button(onClick = { expanded = true}) { }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.IDE_file_create)) },
                    onClick = {
                        dialogTitle = createTitle
                        dialogAction = { fileName ->
                            File(locContext.filesDir, "$fileName.lds").createNewFile()
                            file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "$fileName.lds")
                        }
                        showDialog = true
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.IDE_file_open)) },
                    onClick = {
                        launcher.launch(arrayOf("*/*"))
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.IDE_file_save))},
                    onClick = {
                        file?.writeText(fileContent)
                    }
                )
            }
            if (showDialog) {
                InputTextDialog(
                    title = dialogTitle,
                    onConfirm = { input ->
                        dialogAction(input)
                        dialogAction = { }
                        showDialog = false
                    },
                    onDismiss = {
                        showDialog = false
                        dialogAction = { }
                    }
                )
            }
        }
    }
}