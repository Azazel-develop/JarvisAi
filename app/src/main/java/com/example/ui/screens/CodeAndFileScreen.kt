package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.GeneratedFile
import com.example.ui.JarvisViewModel
import com.example.ui.components.CodeBlockView
import com.example.ui.theme.*

@Composable
fun CodeAndFileScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val generatedFiles by viewModel.generatedFiles.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()

    var activeTab by remember { mutableIntStateOf(0) } // 0 = Workspace & Editor, 1 = Upload File/Folder, 2 = Remote Downloader, 3 = Generator & GitHub

    var selectedLanguage by remember { mutableStateOf("Kotlin") }
    var promptInput by remember { mutableStateOf("") }
    var githubRepoName by remember { mutableStateOf("jarvis-neural-core") }

    val languages = listOf("Kotlin", "Python", "JavaScript", "C++", "Rust", "HTML/CSS", "JSON", "SQL", "YAML")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GalaxyVoid)
            .statusBarsPadding()
            .padding(horizontal = 14.dp)
    ) {
        // Screen Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp)
        ) {
            Column {
                Text(
                    text = "STARK CODE & FILE MATRIX",
                    color = TextGlow,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Workspace • File Upload • Download • AI Modification",
                    color = CosmicCyan,
                    fontSize = 10.sp
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = VoidSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, VoidBorder)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(StatusGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${generatedFiles.size} FILES ACTIVE",
                        color = TextMuted,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Tab Navigation
        TabRow(
            selectedTabIndex = activeTab,
            containerColor = VoidSurface,
            contentColor = CosmicCyan,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                    color = VioletNeon
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, VoidBorder, RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                text = { Text("Workspace (${generatedFiles.size})", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                text = { Text("Upload File/Folder", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = activeTab == 2,
                onClick = { activeTab = 2 },
                text = { Text("Remote Fetch", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = activeTab == 3,
                onClick = { activeTab = 3 },
                text = { Text("AI Generator", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (activeTab) {
            0 -> WorkspaceEditorTab(
                files = generatedFiles,
                isProcessing = isProcessing,
                onSaveFile = { name, lang, content -> viewModel.saveGeneratedFile(name, lang, content) },
                onModifyWithPrompt = { file, prompt, callback -> viewModel.modifyFileWithPrompt(file, prompt, callback) },
                onExportFile = { file -> viewModel.exportFileToDeviceStorage(context, file) },
                onDeleteFile = { viewModel.deleteFile(it) }
            )

            1 -> UploadFileAndFolderTab(
                onImportFiles = { viewModel.importUploadedFiles(it) }
            )

            2 -> RemoteFileDownloaderTab(
                isProcessing = isProcessing,
                onDownloadRemote = { url, name, callback -> viewModel.downloadRemoteFile(url, name, callback) }
            )

            3 -> AiGeneratorAndGitHubTab(
                languages = languages,
                selectedLanguage = selectedLanguage,
                onLanguageSelect = { selectedLanguage = it },
                promptInput = promptInput,
                onPromptChange = { promptInput = it },
                isProcessing = isProcessing,
                githubRepoName = githubRepoName,
                onRepoNameChange = { githubRepoName = it },
                onGenerate = {
                    if (promptInput.isNotBlank()) {
                        viewModel.sendMessage("Generate a complete $selectedLanguage program/document for: $promptInput")
                        promptInput = ""
                    }
                },
                onSaveFile = { name, lang, content -> viewModel.saveGeneratedFile(name, lang, content) }
            )
        }
    }
}

@Composable
fun WorkspaceEditorTab(
    files: List<GeneratedFile>,
    isProcessing: Boolean,
    onSaveFile: (String, String, String) -> Unit,
    onModifyWithPrompt: (GeneratedFile, String, (Boolean, String) -> Unit) -> Unit,
    onExportFile: (GeneratedFile) -> Unit,
    onDeleteFile: (GeneratedFile) -> Unit
) {
    val context = LocalContext.current
    var selectedFileId by remember { mutableStateOf<Long?>(files.firstOrNull()?.id) }
    var searchFilter by remember { mutableStateOf("") }

    val activeFile = files.find { it.id == selectedFileId } ?: files.firstOrNull()

    var isEditingMode by remember { mutableStateOf(false) }
    var editedContent by remember { mutableStateOf("") }
    var aiModifyPrompt by remember { mutableStateOf("") }
    var showAiModifyDialog by remember { mutableStateOf(false) }

    LaunchedEffect(activeFile?.id) {
        if (activeFile != null) {
            editedContent = activeFile.content
            selectedFileId = activeFile.id
        }
    }

    if (files.isEmpty()) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(imageVector = Icons.Default.FolderOpen, contentDescription = "Empty", tint = TextMuted, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text("No files in workspace yet", color = TextGlow, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("Upload a file/folder, download from a URL, or generate new code using the tabs above.", color = TextMuted, fontSize = 11.sp)
            }
        }
        return
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // File Selector Ribbon
        OutlinedTextField(
            value = searchFilter,
            onValueChange = { searchFilter = it },
            placeholder = { Text("Filter workspace files...", color = TextMuted, fontSize = 11.sp) },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = CosmicCyan, modifier = Modifier.size(16.dp)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = VioletNeon,
                unfocusedBorderColor = VoidBorder,
                focusedContainerColor = VoidSurface,
                unfocusedContainerColor = VoidSurface,
                focusedTextColor = TextGlow,
                unfocusedTextColor = TextGlow
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        )

        val filteredFiles = if (searchFilter.isBlank()) files else files.filter { it.fileName.contains(searchFilter, ignoreCase = true) || it.language.contains(searchFilter, ignoreCase = true) }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(filteredFiles) { file ->
                val isSelected = file.id == activeFile?.id
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        selectedFileId = file.id
                        editedContent = file.content
                        isEditingMode = false
                    },
                    label = { Text(text = file.fileName, fontSize = 11.sp, color = if (isSelected) GalaxyVoid else TextGlow, fontWeight = FontWeight.Bold) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = "File",
                            tint = if (isSelected) GalaxyVoid else CosmicCyan,
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = VioletNeon,
                        containerColor = VoidSurface
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }

        if (activeFile != null) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = VoidSurface),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .border(1.dp, VoidBorder, RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Action Header Bar
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(text = activeFile.fileName, color = TextGlow, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(text = "Language: ${activeFile.language} • ${activeFile.content.length} chars", color = CosmicCyan, fontSize = 10.sp)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            // AI Refactor / Modify Button
                            IconButton(onClick = { showAiModifyDialog = !showAiModifyDialog }) {
                                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "AI Modify", tint = PlasmaPink)
                            }

                            // Manual Edit Toggle
                            IconButton(onClick = { isEditingMode = !isEditingMode }) {
                                Icon(imageVector = if (isEditingMode) Icons.Default.Check else Icons.Default.Edit, contentDescription = "Edit", tint = VioletNeon)
                            }

                            // Share / Download to Phone
                            IconButton(onClick = { onExportFile(activeFile) }) {
                                Icon(imageVector = Icons.Default.Download, contentDescription = "Download/Share", tint = StatusGreen)
                            }

                            // Delete File
                            IconButton(onClick = { onDeleteFile(activeFile) }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = StatusRed)
                            }
                        }
                    }

                    // AI Refactor / Modification Panel
                    AnimatedVisibility(visible = showAiModifyDialog) {
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = GalaxyVoid),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .border(1.dp, PlasmaPink, RoundedCornerShape(10.dp))
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "AI Mod", tint = PlasmaPink, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("J.A.R.V.I.S. AI FILE MODIFIER & REFACTOR", color = TextGlow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedTextField(
                                    value = aiModifyPrompt,
                                    onValueChange = { aiModifyPrompt = it },
                                    placeholder = { Text("e.g., Refactor to async, add error handling, translate to Python, fix bugs...", color = TextMuted, fontSize = 10.sp) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = PlasmaPink,
                                        unfocusedBorderColor = VoidBorder,
                                        focusedTextColor = TextGlow,
                                        unfocusedTextColor = TextGlow
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Row(
                                    horizontalArrangement = Arrangement.End,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    TextButton(onClick = { showAiModifyDialog = false }) {
                                        Text("CLOSE", color = TextMuted, fontSize = 10.sp)
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Button(
                                        onClick = {
                                            if (aiModifyPrompt.isNotBlank()) {
                                                onModifyWithPrompt(activeFile, aiModifyPrompt) { success, msg ->
                                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                                    if (success) {
                                                        showAiModifyDialog = false
                                                        aiModifyPrompt = ""
                                                    }
                                                }
                                            }
                                        },
                                        enabled = !isProcessing && aiModifyPrompt.isNotBlank(),
                                        colors = ButtonDefaults.buttonColors(containerColor = PlasmaPink)
                                    ) {
                                        if (isProcessing) {
                                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                                        } else {
                                            Text("EXECUTE MODIFICATION", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (isEditingMode) {
                        Column(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = editedContent,
                                onValueChange = { editedContent = it },
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = TextGlow
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = VioletNeon,
                                    unfocusedBorderColor = VoidBorder,
                                    focusedContainerColor = GalaxyVoid,
                                    unfocusedContainerColor = GalaxyVoid
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    onSaveFile(activeFile.fileName, activeFile.language, editedContent)
                                    isEditingMode = false
                                    Toast.makeText(context, "Saved changes to ${activeFile.fileName}", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(imageVector = Icons.Default.Save, contentDescription = "Save", tint = GalaxyVoid)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("SAVE MANUAL CODE EDITS", color = GalaxyVoid, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    } else {
                        Box(modifier = Modifier.weight(1f)) {
                            CodeBlockView(code = activeFile.content, language = activeFile.language)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UploadFileAndFolderTab(
    onImportFiles: (List<Pair<String, String>>) -> Unit
) {
    val context = LocalContext.current

    // Single / Multiple File Launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (!uris.isNullOrEmpty()) {
            val importedList = mutableListOf<Pair<String, String>>()
            uris.forEach { uri ->
                val fileName = getFileNameFromUri(context, uri)
                val content = readTextFromUri(context, uri)
                importedList.add(Pair(fileName, content))
            }
            onImportFiles(importedList)
            Toast.makeText(context, "Uploaded ${importedList.size} file(s) into Stark Matrix!", Toast.LENGTH_SHORT).show()
        }
    }

    // Folder Launcher (Storage Access Framework)
    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { treeUri ->
        if (treeUri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    treeUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
            val folderFiles = readFolderFromUriTree(context, treeUri)
            if (folderFiles.isNotEmpty()) {
                onImportFiles(folderFiles)
                Toast.makeText(context, "Imported ${folderFiles.size} file(s) from folder!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "No readable text files found in folder", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 90.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = VoidSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, VoidBorder, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.FileUpload, contentDescription = "Upload", tint = CosmicCyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("UPLOAD FILE OR ENTIRE FOLDER", color = TextGlow, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Text(
                        text = "Select any file (code, JSON, script, txt) or pick an entire directory tree from your Android device. J.A.R.V.I.S. will parse, analyze, and allow live modification.",
                        color = TextMuted,
                        fontSize = 11.sp
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = { filePickerLauncher.launch(arrayOf("*/*")) },
                            colors = ButtonDefaults.buttonColors(containerColor = CosmicCyan),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.UploadFile, contentDescription = "File", tint = GalaxyVoid, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("SELECT FILE(S)", color = GalaxyVoid, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { folderPickerLauncher.launch(null) },
                            colors = ButtonDefaults.buttonColors(containerColor = VioletNeon),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.FolderZip, contentDescription = "Folder", tint = TextGlow, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("SELECT FOLDER", color = TextGlow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Text("1-TAP PROJECT FOLDER PRESETS & TEMPLATES", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }

        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = VoidSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, VoidBorder, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("📦 Android Jetpack App Workspace", color = TextGlow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Generates MainActivity.kt, build.gradle.kts, AndroidManifest.xml, and UserViewModel.kt into your workspace folder.", color = TextMuted, fontSize = 10.sp)

                    Button(
                        onClick = {
                            val presetFiles = listOf(
                                Pair("android_app/MainActivity.kt", "package com.stark.app\n\nimport androidx.activity.ComponentActivity\n\nclass MainActivity : ComponentActivity() {\n    override fun onCreate(savedInstanceState: android.os.Bundle?) {\n        super.onCreate(savedInstanceState)\n    }\n}"),
                                Pair("android_app/build.gradle.kts", "plugins {\n    alias(libs.plugins.android.application)\n    alias(libs.plugins.kotlin.compose)\n}\n\nandroid {\n    namespace = \"com.stark.app\"\n}"),
                                Pair("android_app/AndroidManifest.xml", "<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\">\n    <application android:label=\"Stark App\" />\n</manifest>"),
                                Pair("android_app/UserViewModel.kt", "package com.stark.app\n\nimport androidx.lifecycle.ViewModel\n\nclass UserViewModel : ViewModel() {\n    // Neural State Flow\n}")
                            )
                            onImportFiles(presetFiles)
                            Toast.makeText(context, "Android Workspace Folder created!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = VioletNeon),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("IMPORT ANDROID PROJECT FOLDER", color = TextGlow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = VoidSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, VoidBorder, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("🐍 Python FastAPI Microservice Workspace", color = TextGlow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Generates main.py, config.py, database.py, and requirements.txt.", color = TextMuted, fontSize = 10.sp)

                    Button(
                        onClick = {
                            val presetFiles = listOf(
                                Pair("python_api/main.py", "from fastapi import FastAPI\nfrom config import settings\n\napp = FastAPI(title=settings.APP_NAME)\n\n@app.get('/')\ndef root():\n    return {'status': 'Jarvis API Active'}"),
                                Pair("python_api/config.py", "class Settings:\n    APP_NAME: str = 'Stark Fast API'\n    PORT: int = 8000\n\nsettings = Settings()"),
                                Pair("python_api/database.py", "import sqlite3\n\ndef get_db():\n    conn = sqlite3.connect('stark.db')\n    return conn"),
                                Pair("python_api/requirements.txt", "fastapi>=0.100.0\nuvicorn>=0.22.0\npydantic>=2.0")
                            )
                            onImportFiles(presetFiles)
                            Toast.makeText(context, "Python API Workspace Folder created!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("IMPORT PYTHON FASTAPI FOLDER", color = GalaxyVoid, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun RemoteFileDownloaderTab(
    isProcessing: Boolean,
    onDownloadRemote: (String, String?, (Boolean, String) -> Unit) -> Unit
) {
    val context = LocalContext.current
    var urlInput by remember { mutableStateOf("") }
    var customFileName by remember { mutableStateOf("") }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 90.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = VoidSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, VoidBorder, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.CloudDownload, contentDescription = "Download", tint = CosmicCyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("DOWNLOAD FILE DIRECTLY FROM URL (NO UPLOAD NEEDED)", color = TextGlow, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Text("Enter any direct file URL or raw GitHub code link. J.A.R.V.I.S. will download it over HTTP, save it into your workspace, and make it ready for instant AI editing.", color = TextMuted, fontSize = 11.sp)

                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        label = { Text("Direct File URL (https://raw.githubusercontent.com/...)", color = TextMuted, fontSize = 10.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VioletNeon,
                            unfocusedBorderColor = VoidBorder,
                            focusedTextColor = TextGlow,
                            unfocusedTextColor = TextGlow
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = customFileName,
                        onValueChange = { customFileName = it },
                        label = { Text("Custom File Name (Optional, e.g. script.py, config.json)", color = TextMuted, fontSize = 10.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VioletNeon,
                            unfocusedBorderColor = VoidBorder,
                            focusedTextColor = TextGlow,
                            unfocusedTextColor = TextGlow
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            if (urlInput.isNotBlank()) {
                                onDownloadRemote(urlInput.trim(), customFileName.ifBlank { null }) { success, message ->
                                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                    if (success) {
                                        urlInput = ""
                                        customFileName = ""
                                    }
                                }
                            }
                        },
                        enabled = !isProcessing && urlInput.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = CosmicCyan),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(color = GalaxyVoid, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(imageVector = Icons.Default.Download, contentDescription = "Download", tint = GalaxyVoid)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("FETCH & IMPORT REMOTE FILE", color = GalaxyVoid, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        item {
            Text("1-TAP REMOTE SCRIPT DOWNLOAD PRESETS", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }

        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = VoidSurface),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🌐 Raw GitHub Sample Python Script", color = TextGlow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Button(
                        onClick = {
                            urlInput = "https://raw.githubusercontent.com/python/cpython/main/Lib/colorsys.py"
                            customFileName = "colorsys.py"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = VoidBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("LOAD SAMPLE GITHUB URL", color = CosmicCyan, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun AiGeneratorAndGitHubTab(
    languages: List<String>,
    selectedLanguage: String,
    onLanguageSelect: (String) -> Unit,
    promptInput: String,
    onPromptChange: (String) -> Unit,
    isProcessing: Boolean,
    githubRepoName: String,
    onRepoNameChange: (String) -> Unit,
    onGenerate: () -> Unit,
    onSaveFile: (String, String, String) -> Unit
) {
    val context = LocalContext.current

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 90.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = VoidSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, VoidBorder, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("SELECT PROGRAMMING LANGUAGE", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(languages) { lang ->
                            FilterChip(
                                selected = lang == selectedLanguage,
                                onClick = { onLanguageSelect(lang) },
                                label = { Text(text = lang, fontSize = 11.sp, color = TextGlow) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = VioletNeon,
                                    containerColor = VoidSurface
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = promptInput,
                        onValueChange = onPromptChange,
                        placeholder = { Text("Describe code or document to synthesize (e.g. REST API, neural network, HTML canvas)...", color = TextMuted, fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VioletNeon,
                            unfocusedBorderColor = VoidBorder,
                            focusedTextColor = TextGlow,
                            unfocusedTextColor = TextGlow
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                    )

                    Button(
                        onClick = onGenerate,
                        enabled = !isProcessing && promptInput.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = VioletNeon),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(color = GalaxyVoid, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(imageVector = Icons.Default.Code, contentDescription = "Generate", tint = GalaxyVoid)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("SYNTHESIZE CODE & SAVE FILE", color = GalaxyVoid, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = VoidSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, VoidBorder, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Language, contentDescription = "GitHub", tint = CosmicCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("GITHUB REPOSITORY AUTOMATION", color = TextGlow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedTextField(
                        value = githubRepoName,
                        onValueChange = onRepoNameChange,
                        label = { Text("Repository Name", color = TextMuted, fontSize = 10.sp) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VioletNeon, unfocusedBorderColor = VoidBorder, focusedTextColor = TextGlow, unfocusedTextColor = TextGlow),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            val url = "https://github.com/new?name=$githubRepoName"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Cannot launch browser", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CosmicCyan),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("OPEN GITHUB TO CREATE $githubRepoName", color = GalaxyVoid, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private fun readTextFromUri(context: Context, uri: Uri): String {
    return try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            inputStream.bufferedReader().readText()
        } ?: ""
    } catch (e: Exception) {
        "// Error reading file: ${e.localizedMessage}"
    }
}

private fun getFileNameFromUri(context: Context, uri: Uri): String {
    var result: String? = null
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val displayNameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (displayNameIndex != -1) {
                    result = it.getString(displayNameIndex)
                }
            }
        }
    }
    if (result == null) {
        result = uri.path
        val cut = result?.lastIndexOf('/') ?: -1
        if (cut != -1 && result != null) {
            result = result.substring(cut + 1)
        }
    }
    return result ?: "uploaded_file_${System.currentTimeMillis()}.txt"
}

private fun readFolderFromUriTree(context: Context, treeUri: Uri): List<Pair<String, String>> {
    val fileList = mutableListOf<Pair<String, String>>()
    try {
        val documentId = DocumentsContract.getTreeDocumentId(treeUri)
        val dirUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, documentId)
        val cursor = context.contentResolver.query(
            dirUri,
            arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_MIME_TYPE
            ),
            null, null, null
        )
        cursor?.use {
            val idIndex = it.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
            val nameIndex = it.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
            val mimeIndex = it.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE)
            while (it.moveToNext()) {
                val docId = it.getString(idIndex)
                val docName = it.getString(nameIndex) ?: "file.txt"
                val mimeType = it.getString(mimeIndex)
                if (mimeType != DocumentsContract.Document.MIME_TYPE_DIR) {
                    val fileUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
                    val content = context.contentResolver.openInputStream(fileUri)?.use { input ->
                        input.bufferedReader().readText()
                    } ?: ""
                    fileList.add(Pair(docName, content))
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return fileList
}
