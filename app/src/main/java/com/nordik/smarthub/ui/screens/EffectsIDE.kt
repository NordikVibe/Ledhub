package com.nordik.smarthub.ui.screens

import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Text
// ...existing code... (удалён неиспользуемый импорт TextField)
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import com.nordik.smarthub.R
import java.io.File

import com.nordik.smarthub.ui.InputTextDialog

//======================================================================================================
val commands = mapOf(
    "fill" to 0x0103,
    "brightness" to 0x0201,
    "wait" to 0x0301
)
//======================================================================================================

fun highlight(code: String): AnnotatedString {
    val keywords = commands.keys

    return buildAnnotatedString {
        code.lines().forEach { line ->
            var start = 0
            val words = Regex("\\b\\w+\\b").findAll(line)
            for (match in words) {
                val word = match.value
                val range = match.range
                append(line.substring(start, range.first))
                if (word in keywords) {
                    withStyle(SpanStyle(color = Color(0xFF569CD6))) {
                        append(word)
                    }
                } else if (word.isDigitsOnly()) {
                    withStyle(SpanStyle(color = Color(0xFFB5CEA8))) {
                        append(word)
                    }
                }
                else {
                    append(word)
                }
                start = range.last + 1
            }
            if (start < line.length) {
                append(line.substring(start))
            }
            append("\n")
        }
    }
}
@Composable
fun EffectsEdit() {
    var expanded by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var file by remember { mutableStateOf<File?>(null) }
    var dialogTitle by remember { mutableStateOf<String>("") }
    var dialogAction by remember { mutableStateOf<(String) -> Unit>({}) }
    var fileContent by remember { mutableStateOf("") }

    val highlightedFileContent = remember(fileContent) { highlight(fileContent) }

    val createTitle = stringResource(R.string.IDE_file_create_title)

    val scrollState = rememberScrollState()

    val locContext = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            fileContent = locContext.contentResolver.openInputStream(it)
                ?.bufferedReader()
                ?.readText() ?: ""
        }
    }
    Column(modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight()) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .height(20.dp)) {
                Spacer(Modifier.weight(1f))
                Button(
                    modifier = Modifier.width(40.dp).height(80.dp),
                    onClick = { expanded = true},
                    shape = RoundedCornerShape(50))
                {
                    Icon(Icons.Default.Menu, "")
                }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.IDE_file_create)) },
                    onClick = {
                        dialogTitle = createTitle
                        dialogAction = { fileName ->
                            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "$fileName.lds").createNewFile()
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
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(0.dp, 6.dp)
            .border(2.dp, MaterialTheme.colorScheme.primary, RectangleShape)
            .verticalScroll(scrollState)){
            BasicTextField(
                value = fileContent,
                //value = highlight(fileContent),
                onValueChange = { fileContent = it },
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                textStyle = TextStyle(fontFamily = FontFamily.Monospace, color = Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(6.dp),
                decorationBox = { innerTextField ->
                    Row(verticalAlignment = Alignment.Top) {
                        Column(Modifier.padding(6.dp)) {
                            fileContent.lines().forEachIndexed { index, _ ->
                                Text(
                                    text = "${index + 1}",
                                    style = TextStyle(
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
                                    )
                                )
                            }
                        }
                        Column(modifier = Modifier
                            .padding(start = 6.dp, top = 6.dp, bottom = 6.dp)
                            .weight(1f))
                        {
                            Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                                Text(
                                    highlightedFileContent,
                                    style = TextStyle(fontFamily = FontFamily.Monospace)
                                )
                                Box(modifier = Modifier.matchParentSize()) {
                                    innerTextField()
                                }
                            }

                        }
                    }
                }
            )
        }
    }
}