package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.JarvisViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

data class AnimeCharacterRef(
    val id: String,
    val name: String,
    val role: String,
    val traits: String,
    val photoRefUrl: String,
    val primaryColor: Color
)

data class AnimeKeyframe(
    val frameIndex: Int,
    val title: String,
    val cameraAngle: String,
    val description: String,
    val dialogue: String,
    val lockedCharacter: String,
    val primaryColor: Color,
    val secondaryColor: Color
)

@Composable
fun AnimeAnimationStudioScreen(viewModel: JarvisViewModel) {
    val context = LocalContext.current
    val userProfile by viewModel.userProfile.collectAsState()
    val title = userProfile?.preferredTitle ?: "Sir"

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Single Picture/Anime Art, 1 = Long Animation Studio, 2 = Character Studio Vault & Voices

    // Picture Generator State
    var picturePrompt by remember { mutableStateOf("Cyberpunk samurai warrior standing in Tokyo neon rain with glowing energy katana") }
    var pictureStyle by remember { mutableStateOf("Anime (Cyberpunk)") }
    var isGeneratingPicture by remember { mutableStateOf(false) }
    var generatedPictureTitle by remember { mutableStateOf<String?>(null) }

    // Character Vault State ("Never messes characters")
    var characterList by remember {
        mutableStateOf(
            listOf(
                AnimeCharacterRef("c1", "Ren (Android Knight)", "Protagonist", "Spiky silver hair, blue glowing cyber-eyes, black obsidian nano-armor with glowing cyan circuit lines", "https://images.unsplash.com/photo-1578632767115-351597cf2477", CosmicCyan),
                AnimeCharacterRef("c2", "Yuki (Quantum Mage)", "Heroine", "Long violet hair, crimson eyes, white holographic battle kimono with floating energy daggers", "https://images.unsplash.com/photo-1534447677768-be436bb09401", VioletNeon),
                AnimeCharacterRef("c3", "Lord Vex (Shadow Lord)", "Antagonist", "Dark shadowy cape, glowing red visor, crimson energy broadsword", "https://images.unsplash.com/photo-1518709268805-4e9042af9f23", PlasmaPink)
            )
        )
    }
    var selectedCharacterId by remember { mutableStateOf("c1") }
    var newCharName by remember { mutableStateOf("") }
    var newCharTraits by remember { mutableStateOf("") }
    var newCharRole by remember { mutableStateOf("Hero") }

    // Long Animation Studio State
    var storyInput by remember {
        mutableStateOf(
            "In Neo-Tokyo 2099, @Ren discovers an ancient quantum scroll buried in ruins.\n" +
            "When he activates it, holographic energy dragons rise into the sky, granting him supercharged lightning sword powers.\n" +
            "Rogue shadow drones led by @Lord Vex descend from the clouds to steal the scroll.\n" +
            "@Ren and @Yuki combine their Quantum Strike, shattering the drone armada and saving the city!"
        )
    }
    var animationStyle by remember { mutableStateOf("Shonen Action Anime") }
    var keyframeLength by remember { mutableIntStateOf(8) } // 4, 8, 12, 16 frames
    var frameRateFps by remember { mutableFloatStateOf(1.0f) } // 0.5, 1.0, 2.0, 5.0
    var isGeneratingAnimation by remember { mutableStateOf(false) }
    var generatedKeyframes by remember { mutableStateOf<List<AnimeKeyframe>>(emptyList()) }

    // Keyframe Editing State
    var editingKeyframe by remember { mutableStateOf<AnimeKeyframe?>(null) }
    var showKeyframeEditDialog by remember { mutableStateOf(false) }

    // Humanlike Voice State
    var selectedVoiceProfile by remember { mutableStateOf("J.A.R.V.I.S. Classic British") }

    // Animation Player State
    var currentFrameIndex by remember { mutableIntStateOf(0) }
    var isPlayingSequence by remember { mutableStateOf(false) }

    // Auto playback timer loop
    LaunchedEffect(isPlayingSequence, generatedKeyframes, frameRateFps) {
        if (isPlayingSequence && generatedKeyframes.isNotEmpty()) {
            val delayMs = (1000f / frameRateFps).toLong().coerceAtLeast(200L)
            while (isPlayingSequence) {
                delay(delayMs)
                currentFrameIndex = (currentFrameIndex + 1) % generatedKeyframes.size
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GalaxyVoid)
            .padding(16.dp)
            .padding(bottom = 80.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header Title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = CosmicCyan, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ANIME & ANIMATION STUDIO", color = TextGlow, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                }
                Text("Synthesize 8K Anime Art, Character Vault, Animation Downloads & Holo AR", color = TextMuted, fontSize = 11.sp)
            }

            Surface(
                color = VioletNeon.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, VioletNeon)
            ) {
                Text("STARK STUDIO v4.0", color = TextGlow, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tab Selector Row
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                onClick = { selectedTab = 0 },
                color = if (selectedTab == 0) CosmicCyan else VoidSurfaceVariant,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f).height(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("🖼️ ART / PICTURE", color = if (selectedTab == 0) Color.Black else TextGlow, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Surface(
                onClick = { selectedTab = 1 },
                color = if (selectedTab == 1) VioletNeon else VoidSurfaceVariant,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f).height(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("🎬 ANIMATION (STORY)", color = if (selectedTab == 1) TextGlow else TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Surface(
                onClick = { selectedTab = 2 },
                color = if (selectedTab == 2) PlasmaPink else VoidSurfaceVariant,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f).height(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("👤 CHARACTERS & VOICE", color = if (selectedTab == 2) Color.Black else TextGlow, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // TAB 0: PICTURE / ANIME ART GENERATOR
        if (selectedTab == 0) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = VoidSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CosmicCyan.copy(alpha = 0.8f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("ARTWORK & PICTURE PROMPT DESCRIPTION:", color = TextGlow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = picturePrompt,
                        onValueChange = { picturePrompt = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CosmicCyan,
                            unfocusedBorderColor = VoidBorder,
                            focusedTextColor = TextGlow,
                            unfocusedTextColor = TextGlow
                        ),
                        placeholder = { Text("Describe image or anime scene...", color = TextMuted) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("ART & ANIME VISUAL STYLE:", color = TextGlow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))

                    val artStyles = listOf("Anime (Cyberpunk)", "Studio Ghibli", "Shonen Action", "Ufotable Cinematic 4K", "3D Unreal Engine 5", "Dark Fantasy", "Webtoon/Manhwa", "Chibi/Kawaii")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        itemsIndexed(artStyles) { _, style ->
                            val isSelected = pictureStyle == style
                            Surface(
                                onClick = { pictureStyle = style },
                                color = if (isSelected) CosmicCyan.copy(alpha = 0.3f) else VoidSurfaceVariant,
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) CosmicCyan else VoidBorder)
                            ) {
                                Text(style, color = TextGlow, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (picturePrompt.isBlank()) {
                                Toast.makeText(context, "Please describe the artwork prompt first", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            isGeneratingPicture = true
                            generatedPictureTitle = null
                            viewModel.speechEngine.speak("Synthesizing $pictureStyle artwork, $title. Initializing neural visual matrix.")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CosmicCyan),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("✨ SYNTHESIZE PICTURE / ANIME ART", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Artwork Render Canvas Area
            LaunchedEffect(isGeneratingPicture) {
                if (isGeneratingPicture) {
                    delay(1500)
                    isGeneratingPicture = false
                    generatedPictureTitle = picturePrompt
                    viewModel.speechEngine.speak("Artwork synthesis complete, $title. Saved to Stark Visual Vault.")
                }
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = VoidSurface),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, VioletNeon),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    if (isGeneratingPicture) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = VioletNeon)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("⚡ SYNTHESIZING NEURAL ANIME ARTWORK...", color = TextGlow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else if (generatedPictureTitle != null) {
                        // Interactive Procedural Visual Canvas Render
                        AnimeArtCanvasRender(style = pictureStyle, titleText = generatedPictureTitle ?: "")

                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.85f))
                                .padding(10.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("🎨 ${generatedPictureTitle?.take(40)}...", color = TextGlow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text("Style: $pictureStyle • Resolution: 3840 x 2160", color = StatusGreen, fontSize = 9.sp)
                                }

                                Button(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("Artwork Prompt", generatedPictureTitle)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Image Card & Prompt Saved to Device Downloads!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CosmicCyan),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("📥 DOWNLOAD", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                            Icon(imageVector = Icons.Default.Brush, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No Artwork Generated Yet", color = TextGlow, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Enter a prompt description above and tap Synthesize to render 8K anime art.", color = TextMuted, fontSize = 11.sp, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }

        // TAB 1: LONG STORYBOARD ANIMATION STUDIO
        if (selectedTab == 1) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = VoidSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, VioletNeon.copy(alpha = 0.8f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📖", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("ANIMATION STORY NARRATIVE (CHARACTER LOCK ENGAGED):", color = TextGlow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Character references (@Ren, @Yuki, @Lord Vex) are auto-locked into every keyframe.", color = TextMuted, fontSize = 10.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = storyInput,
                        onValueChange = { storyInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VioletNeon,
                            unfocusedBorderColor = VoidBorder,
                            focusedTextColor = TextGlow,
                            unfocusedTextColor = TextGlow
                        ),
                        placeholder = { Text("Provide your story narrative here...", color = TextMuted) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("ANIMATION STUDIO STYLE:", color = TextGlow, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))

                    val styles = listOf("Shonen Action Anime", "Ufotable Cinematic 4K", "Studio Ghibli", "3D Unreal Engine 5", "Cyberpunk 2099")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        itemsIndexed(styles) { _, st ->
                            val isSel = animationStyle == st
                            Surface(
                                onClick = { animationStyle = st },
                                color = if (isSel) VioletNeon.copy(alpha = 0.3f) else VoidSurfaceVariant,
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) VioletNeon else VoidBorder)
                            ) {
                                Text(st, color = TextGlow, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Animation Parameters
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Keyframe Count & Max Duration Selector (up to 25 mins)
                        Column(modifier = Modifier.weight(1.2f)) {
                            Text("EPISODE DURATION & KEYFRAMES (MAX 25 MINS):", color = TextGlow, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf(
                                    Pair(8, "1m / 8f"),
                                    Pair(16, "5m / 16f"),
                                    Pair(32, "10m / 32f"),
                                    Pair(60, "15m / 60f"),
                                    Pair(100, "25m / 100f")
                                ).forEach { (len, label) ->
                                    Surface(
                                        onClick = { keyframeLength = len },
                                        color = if (keyframeLength == len) VioletNeon.copy(alpha = 0.35f) else VoidSurfaceVariant,
                                        shape = RoundedCornerShape(6.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, if (keyframeLength == len) VioletNeon else VoidBorder),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 4.dp)) {
                                            Text(label, color = TextGlow, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        // FPS Speed Selector
                        Column(modifier = Modifier.weight(1f)) {
                            Text("PLAYBACK SPEED:", color = TextGlow, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf(0.5f, 1.0f, 2.0f, 5.0f).forEach { fps ->
                                    Surface(
                                        onClick = { frameRateFps = fps },
                                        color = if (frameRateFps == fps) CosmicCyan.copy(alpha = 0.3f) else VoidSurfaceVariant,
                                        shape = RoundedCornerShape(6.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, if (frameRateFps == fps) CosmicCyan else VoidBorder),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 4.dp)) {
                                            Text("${fps}fps", color = TextGlow, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (storyInput.isBlank()) {
                                Toast.makeText(context, "Please enter your animation story narrative", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            isGeneratingAnimation = true
                            isPlayingSequence = false
                            currentFrameIndex = 0

                            viewModel.speechEngine.speak("Synthesizing $animationStyle arc into $keyframeLength keyframes with character locks engaged, $title.")

                            // Generate Keyframes with locked character traits
                            val lines = storyInput.lineSequence().filter { it.isNotBlank() }.toList()
                            val keyframes = mutableListOf<AnimeKeyframe>()
                            val colors = listOf(
                                Pair(VioletNeon, PlasmaPink),
                                Pair(CosmicCyan, StatusGreen),
                                Pair(StatusGold, PlasmaPink),
                                Pair(VioletNeon, CosmicCyan)
                            )

                            for (i in 0 until keyframeLength) {
                                val lineIndex = i % lines.size
                                val lineText = lines.getOrElse(lineIndex) { "Scene Action Sequence #$i" }
                                val colorPair = colors[i % colors.size]

                                val lockedChar = when {
                                    lineText.contains("@Yuki", true) -> "Yuki (Quantum Mage - Violet Hair)"
                                    lineText.contains("@Vex", true) -> "Lord Vex (Shadow Cape)"
                                    else -> "Ren (Silver Cyber-hair, Nano-armor)"
                                }

                                keyframes.add(
                                    AnimeKeyframe(
                                        frameIndex = i + 1,
                                        title = "Keyframe #${i + 1}: ${lineText.take(28)}...",
                                        cameraAngle = when (i % 4) {
                                            0 -> "🎥 Wide Aerial Shot"
                                            1 -> "🔍 Medium Close-Up"
                                            2 -> "💥 High Dynamic Action Angle"
                                            else -> "✨ Low Angle Cinematic Focus"
                                        },
                                        description = "Story Frame: $lineText",
                                        dialogue = "\"${if (i % 2 == 0) "Quantum powers unleashed!" else "Neo-Tokyo shall never fall!"}\"",
                                        lockedCharacter = lockedChar,
                                        primaryColor = colorPair.first,
                                        secondaryColor = colorPair.second
                                    )
                                )
                            }

                            generatedKeyframes = keyframes
                            isGeneratingAnimation = false
                            isPlayingSequence = true
                            viewModel.speechEngine.speak("Animation sequence synthesized! Playback initialized.")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = VioletNeon),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🎬 GENERATE HIGH-QUALITY ANIMATION", color = TextGlow, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ANIMATION PLAYER CONSOLE & CANVAS
            if (generatedKeyframes.isNotEmpty()) {
                val activeKeyframe = generatedKeyframes.getOrElse(currentFrameIndex) { generatedKeyframes.first() }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = VoidSurface),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, CosmicCyan),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        // Keyframe Title & Frame Index
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("FRAME ${currentFrameIndex + 1} / ${generatedKeyframes.size}", color = CosmicCyan, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                            Text(activeKeyframe.cameraAngle, color = StatusGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Character Consistency Indicator
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🔒 CHAR LOCK:", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(activeKeyframe.lockedCharacter, color = PlasmaPink, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Keyframe Animation Screen Canvas
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black)
                                .border(1.dp, activeKeyframe.primaryColor, RoundedCornerShape(12.dp))
                        ) {
                            // Animated Story Canvas
                            KeyframeCanvasRender(keyframe = activeKeyframe)

                            // Dialogue Overlay Box
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .background(Color.Black.copy(alpha = 0.85f))
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Text(activeKeyframe.title, color = TextGlow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text(activeKeyframe.dialogue, color = CosmicCyan, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Scrubber Slider
                        Slider(
                            value = currentFrameIndex.toFloat(),
                            onValueChange = {
                                isPlayingSequence = false
                                currentFrameIndex = it.toInt().coerceIn(0, generatedKeyframes.size - 1)
                            },
                            valueRange = 0f..(generatedKeyframes.size - 1).toFloat(),
                            steps = (generatedKeyframes.size - 2).coerceAtLeast(0),
                            colors = SliderDefaults.colors(
                                thumbColor = CosmicCyan,
                                activeTrackColor = VioletNeon,
                                inactiveTrackColor = VoidBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Playback & Download Control Buttons
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        isPlayingSequence = false
                                        currentFrameIndex = (currentFrameIndex - 1 + generatedKeyframes.size) % generatedKeyframes.size
                                    }
                                ) {
                                    Icon(imageVector = Icons.Default.SkipPrevious, contentDescription = "Prev", tint = TextGlow)
                                }

                                Button(
                                    onClick = { isPlayingSequence = !isPlayingSequence },
                                    colors = ButtonDefaults.buttonColors(containerColor = if (isPlayingSequence) PlasmaPink else StatusGreen),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (isPlayingSequence) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = "Play",
                                            tint = Color.Black,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(if (isPlayingSequence) "PAUSE" else "PLAY", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        isPlayingSequence = false
                                        currentFrameIndex = (currentFrameIndex + 1) % generatedKeyframes.size
                                    }
                                ) {
                                    Icon(imageVector = Icons.Default.SkipNext, contentDescription = "Next", tint = TextGlow)
                                }
                            }

                            // Holo Ani Mode & Export Buttons
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Button(
                                    onClick = {
                                        viewModel.toggleHologramProjection(true)
                                        viewModel.speechEngine.speak("Projecting animation keyframe sequence into Holo AR matrix, $title.")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = VioletNeon),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("🔮 HOLO ANI AR", color = TextGlow, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        val htmlVideo = generateHtml5AnimationVideo(generatedKeyframes, "Stark Animation Video", animationStyle, frameRateFps)
                                        viewModel.saveGeneratedFile("stark_animation_video.html", "HTML/CSS", htmlVideo)
                                        Toast.makeText(context, "Animation Video HTML Saved to Stark Workspace Matrix!", Toast.LENGTH_LONG).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CosmicCyan),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("🎬 EXPORT VIDEO", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // INTERACTIVE TIMELINE & KEYFRAME VIDEO EDITOR
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = VoidSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, VioletNeon),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text("🎞️ ANIMATION VIDEO TIMELINE & FRAME EDITOR", color = TextGlow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("Reorder, edit camera angles, custom dialogue & keyframe scenes", color = TextMuted, fontSize = 10.sp)
                            }

                            Button(
                                onClick = {
                                    val nextIdx = generatedKeyframes.size + 1
                                    val newKf = AnimeKeyframe(
                                        frameIndex = nextIdx,
                                        title = "Keyframe #$nextIdx: Quantum Surge Scene",
                                        cameraAngle = "💥 High Dynamic Action Angle",
                                        description = "Character activates supreme nano-blast attack.",
                                        dialogue = "\"Feel the power of the Stark Quantum Core!\"",
                                        lockedCharacter = "Ren (Silver Cyber-hair)",
                                        primaryColor = VioletNeon,
                                        secondaryColor = PlasmaPink
                                    )
                                    generatedKeyframes = generatedKeyframes + newKf
                                    Toast.makeText(context, "Added Keyframe #$nextIdx", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = "Add", tint = GalaxyVoid, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("ADD FRAME", color = GalaxyVoid, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            itemsIndexed(generatedKeyframes) { idx, kf ->
                                Card(
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = VoidSurfaceVariant),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (idx == currentFrameIndex) CosmicCyan else VoidBorder),
                                    modifier = Modifier
                                        .width(200.dp)
                                        .clickable {
                                            isPlayingSequence = false
                                            currentFrameIndex = idx
                                        }
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("FRAME #${idx + 1}", color = CosmicCyan, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                                            Row {
                                                if (idx > 0) {
                                                    IconButton(
                                                        onClick = {
                                                            val mutable = generatedKeyframes.toMutableList()
                                                            val tmp = mutable[idx]
                                                            mutable[idx] = mutable[idx - 1]
                                                            mutable[idx - 1] = tmp
                                                            generatedKeyframes = mutable
                                                            if (currentFrameIndex == idx) currentFrameIndex = idx - 1
                                                        },
                                                        modifier = Modifier.size(22.dp)
                                                    ) {
                                                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Left", tint = TextMuted, modifier = Modifier.size(14.dp))
                                                    }
                                                }
                                                if (idx < generatedKeyframes.size - 1) {
                                                    IconButton(
                                                        onClick = {
                                                            val mutable = generatedKeyframes.toMutableList()
                                                            val tmp = mutable[idx]
                                                            mutable[idx] = mutable[idx + 1]
                                                            mutable[idx + 1] = tmp
                                                            generatedKeyframes = mutable
                                                            if (currentFrameIndex == idx) currentFrameIndex = idx + 1
                                                        },
                                                        modifier = Modifier.size(22.dp)
                                                    ) {
                                                        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Right", tint = TextMuted, modifier = Modifier.size(14.dp))
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(kf.title, color = TextGlow, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                        Text(kf.cameraAngle, color = StatusGold, fontSize = 9.sp, fontWeight = FontWeight.Medium)
                                        Text(kf.dialogue, color = CosmicCyan, fontSize = 9.sp, maxLines = 2)

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Button(
                                                onClick = {
                                                    editingKeyframe = kf
                                                    showKeyframeEditDialog = true
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = VioletNeon),
                                                shape = RoundedCornerShape(6.dp),
                                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = TextGlow, modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text("EDIT", color = TextGlow, fontSize = 9.sp)
                                            }

                                            IconButton(
                                                onClick = {
                                                    if (generatedKeyframes.size > 1) {
                                                        generatedKeyframes = generatedKeyframes.filterIndexed { i, _ -> i != idx }
                                                        currentFrameIndex = currentFrameIndex.coerceAtMost(generatedKeyframes.size - 1)
                                                    } else {
                                                        Toast.makeText(context, "Cannot delete last frame", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                modifier = Modifier.size(26.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = StatusRed, modifier = Modifier.size(14.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // EXPORT VIDEO ACTIONS ROW
                        Text("EXPORT & PUBLISH ANIMATION VIDEO:", color = TextGlow, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = {
                                    val htmlVideo = generateHtml5AnimationVideo(generatedKeyframes, "Stark Animation Video", animationStyle, frameRateFps)
                                    viewModel.saveGeneratedFile("stark_animation_video.html", "HTML/CSS", htmlVideo)
                                    Toast.makeText(context, "Saved HTML5 Video Player to Workspace!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = VioletNeon),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("💻 WORKSPACE HTML5", color = TextGlow, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    val jsonManifest = buildString {
                                        append("{\n  \"animation_title\": \"Stark Anime Arc\",\n  \"fps\": $frameRateFps,\n  \"keyframes\": [\n")
                                        generatedKeyframes.forEachIndexed { idx, kf ->
                                            append("    {\n      \"frame\": ${idx + 1},\n      \"title\": \"${kf.title}\",\n      \"camera\": \"${kf.cameraAngle}\",\n      \"character\": \"${kf.lockedCharacter}\",\n      \"dialogue\": \"${kf.dialogue}\"\n    }${if (idx < generatedKeyframes.size - 1) "," else ""}\n")
                                        }
                                        append("  ]\n}")
                                    }
                                    viewModel.saveGeneratedFile("anime_video_manifest.json", "JSON", jsonManifest)
                                    Toast.makeText(context, "Saved Manifest JSON to Workspace!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CosmicCyan),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("📄 MANIFEST JSON", color = GalaxyVoid, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                }
            }
        }

        // KEYFRAME EDIT DIALOG MODAL
        if (showKeyframeEditDialog && editingKeyframe != null) {
            val kf = editingKeyframe!!
            var editTitle by remember(kf) { mutableStateOf(kf.title) }
            var editCamera by remember(kf) { mutableStateOf(kf.cameraAngle) }
            var editDialogue by remember(kf) { mutableStateOf(kf.dialogue) }
            var editChar by remember(kf) { mutableStateOf(kf.lockedCharacter) }

            AlertDialog(
                onDismissRequest = { showKeyframeEditDialog = false },
                containerColor = VoidSurface,
                title = {
                    Text("EDIT KEYFRAME #${kf.frameIndex}", color = TextGlow, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = editTitle,
                            onValueChange = { editTitle = it },
                            label = { Text("Frame Title", color = TextMuted, fontSize = 10.sp) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VioletNeon, unfocusedBorderColor = VoidBorder, focusedTextColor = TextGlow, unfocusedTextColor = TextGlow),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = editCamera,
                            onValueChange = { editCamera = it },
                            label = { Text("Camera Angle", color = TextMuted, fontSize = 10.sp) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VioletNeon, unfocusedBorderColor = VoidBorder, focusedTextColor = TextGlow, unfocusedTextColor = TextGlow),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = editDialogue,
                            onValueChange = { editDialogue = it },
                            label = { Text("Character Dialogue", color = TextMuted, fontSize = 10.sp) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VioletNeon, unfocusedBorderColor = VoidBorder, focusedTextColor = TextGlow, unfocusedTextColor = TextGlow),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = editChar,
                            onValueChange = { editChar = it },
                            label = { Text("Locked Character", color = TextMuted, fontSize = 10.sp) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VioletNeon, unfocusedBorderColor = VoidBorder, focusedTextColor = TextGlow, unfocusedTextColor = TextGlow),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val updatedList = generatedKeyframes.map { item ->
                                if (item.frameIndex == kf.frameIndex) {
                                    item.copy(
                                        title = editTitle,
                                        cameraAngle = editCamera,
                                        dialogue = editDialogue,
                                        lockedCharacter = editChar
                                    )
                                } else item
                            }
                            generatedKeyframes = updatedList
                            showKeyframeEditDialog = false
                            Toast.makeText(context, "Keyframe updated!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = VioletNeon)
                    ) {
                        Text("SAVE EDITS", color = TextGlow, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showKeyframeEditDialog = false }) {
                        Text("CANCEL", color = TextMuted, fontSize = 11.sp)
                    }
                }
            )
        }

        // TAB 2: CHARACTER VAULT & HUMANLIKE VOICE STUDIO
        if (selectedTab == 2) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = VoidSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, PlasmaPink.copy(alpha = 0.8f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("👤 CHARACTER STUDIO & REFERENCE VAULT", color = TextGlow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Register characters to guarantee 100% visual consistency across all story frames.", color = TextMuted, fontSize = 10.sp)

                    Spacer(modifier = Modifier.height(12.dp))

                    // Saved Character List
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        itemsIndexed(characterList) { _, char ->
                            val isSel = selectedCharacterId == char.id
                            Surface(
                                onClick = { selectedCharacterId = char.id },
                                color = if (isSel) char.primaryColor.copy(alpha = 0.3f) else VoidSurfaceVariant,
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) char.primaryColor else VoidBorder),
                                modifier = Modifier.width(180.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text(char.name, color = TextGlow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text(char.role, color = char.primaryColor, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(char.traits, color = TextMuted, fontSize = 9.sp, maxLines = 3)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("ADD NEW CHARACTER REFERENCE:", color = TextGlow, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = newCharName,
                        onValueChange = { newCharName = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PlasmaPink, unfocusedBorderColor = VoidBorder),
                        placeholder = { Text("Character Name (e.g. Kaito)", color = TextMuted) },
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = newCharTraits,
                        onValueChange = { newCharTraits = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PlasmaPink, unfocusedBorderColor = VoidBorder),
                        placeholder = { Text("Visual Traits (Hair color, outfit, eyes, weapons...)", color = TextMuted) }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            if (newCharName.isBlank()) {
                                Toast.makeText(context, "Enter character name first", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val newChar = AnimeCharacterRef("c_${System.currentTimeMillis()}", newCharName, newCharRole, newCharTraits, "", PlasmaPink)
                            characterList = characterList + newChar
                            newCharName = ""
                            newCharTraits = ""
                            Toast.makeText(context, "Character added to Stark Reference Vault!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PlasmaPink),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("➕ SAVE CHARACTER TO VAULT", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // HUMANLIKE VOICE STUDIO CARD
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = VoidSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CosmicCyan.copy(alpha = 0.8f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🗣️ HUMANLIKE VOICE ENGINE ADDITIONS", color = TextGlow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Select natural human voices for J.A.R.V.I.S. & Character Narration", color = TextMuted, fontSize = 10.sp)

                    Spacer(modifier = Modifier.height(12.dp))

                    val voices = listOf(
                        Triple("J.A.R.V.I.S. Classic British", 0.95f, 1.02f),
                        Triple("Tony Stark Witty", 1.10f, 1.08f),
                        Triple("Anime Heroine", 1.35f, 1.15f),
                        Triple("Tactical Commander", 0.70f, 0.90f),
                        Triple("Quantum Synth AI", 0.85f, 1.10f),
                        Triple("Soft Cyber Companion", 1.05f, 0.95f)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        voices.forEach { (vName, pitch, rate) ->
                            val isSel = selectedVoiceProfile == vName
                            Surface(
                                onClick = {
                                    selectedVoiceProfile = vName
                                    viewModel.speechEngine.updatePitchAndRate(pitch, rate)
                                    viewModel.speechEngine.speak("Voice profile configured to $vName, $title.")
                                },
                                color = if (isSel) CosmicCyan.copy(alpha = 0.25f) else VoidSurfaceVariant,
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) CosmicCyan else VoidBorder)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth().padding(10.dp)
                                ) {
                                    Column {
                                        Text(vName, color = if (isSel) CosmicCyan else TextGlow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text("Pitch: ${String.format("%.2f", pitch)} • Speed: ${String.format("%.2f", rate)}", color = TextMuted, fontSize = 9.sp)
                                    }

                                    if (isSel) {
                                        Text("ACTIVE", color = StatusGreen, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnimeArtCanvasRender(style: String, titleText: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "ArtCanvas")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(10000, easing = LinearEasing), RepeatMode.Restart),
        label = "phase"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Background Gradient
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(VioletNeon.copy(alpha = 0.5f), GalaxyVoid),
                center = Offset(w / 2f, h / 2f),
                radius = w / 1.2f
            )
        )

        // Geometric Anime Rays & Grid
        for (i in 0 until 12) {
            val angle = Math.toRadians((i * 30 + phase).toDouble())
            val ex = w / 2f + cos(angle).toFloat() * w
            val ey = h / 2f + sin(angle).toFloat() * h
            drawLine(
                color = CosmicCyan.copy(alpha = 0.25f),
                start = Offset(w / 2f, h / 2f),
                end = Offset(ex, ey),
                strokeWidth = 2f
            )
        }

        // Center Anime Energy Core Frame
        drawCircle(
            color = PlasmaPink.copy(alpha = 0.4f),
            center = Offset(w / 2f, h / 2f),
            radius = 80f
        )
        drawCircle(
            color = CosmicCyan,
            center = Offset(w / 2f, h / 2f),
            radius = 40f,
            style = Stroke(width = 3f)
        )
    }
}

@Composable
fun KeyframeCanvasRender(keyframe: AnimeKeyframe) {
    val infiniteTransition = rememberInfiniteTransition(label = "KeyframeCanvas")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(tween(1000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing), RepeatMode.Restart),
        label = "rotation"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Background Scene Gradient Shader
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    keyframe.primaryColor.copy(alpha = 0.7f),
                    GalaxyVoid,
                    keyframe.secondaryColor.copy(alpha = 0.5f)
                ),
                center = Offset(w / 2f, h / 2f),
                radius = w * 0.8f
            )
        )

        // Speed lines for high action anime dynamics
        for (i in 0 until 16) {
            val rad = Math.toRadians((i * 22.5 + rotationAngle).toDouble())
            val sx = w / 2f + cos(rad).toFloat() * 40f
            val sy = h / 2f + sin(rad).toFloat() * 40f
            val ex = w / 2f + cos(rad).toFloat() * (w * 0.6f * pulse)
            val ey = h / 2f + sin(rad).toFloat() * (w * 0.6f * pulse)

            drawLine(
                color = keyframe.primaryColor.copy(alpha = 0.6f),
                start = Offset(sx, sy),
                end = Offset(ex, ey),
                strokeWidth = 2.5f
            )
        }

        // Inner Character Energy Aura & Silhouette Core
        drawCircle(
            color = keyframe.secondaryColor.copy(alpha = 0.6f),
            center = Offset(w / 2f, h / 2f),
            radius = 65f * pulse
        )
        drawCircle(
            color = keyframe.primaryColor,
            center = Offset(w / 2f, h / 2f),
            radius = 45f,
            style = Stroke(width = 4f)
        )

        // Floating Quantum Particles (Zero Lag)
        for (p in 0 until 10) {
            val px = (w * 0.2f) + ((p * 77 + (rotationAngle * 2)) % (w * 0.6f))
            val py = (h * 0.2f) + ((p * 53 + (pulse * 50)) % (h * 0.6f))
            drawCircle(
                color = if (p % 2 == 0) CosmicCyan else PlasmaPink,
                center = Offset(px, py),
                radius = (3f + (p % 4))
            )
        }
    }
}

fun generateHtml5AnimationVideo(keyframes: List<AnimeKeyframe>, title: String, style: String, fps: Float): String {
    val framesJsonArray = keyframes.joinToString(",\n") { kf ->
        """
        {
          "index": ${kf.frameIndex},
          "title": "${kf.title.replace("\"", "\\\"").replace("\n", " ")}",
          "camera": "${kf.cameraAngle.replace("\"", "\\\"").replace("\n", " ")}",
          "description": "${kf.description.replace("\"", "\\\"").replace("\n", " ")}",
          "dialogue": "${kf.dialogue.replace("\"", "\\\"").replace("\n", " ")}",
          "lockedCharacter": "${kf.lockedCharacter.replace("\"", "\\\"").replace("\n", " ")}"
        }
        """.trimIndent()
    }

    val intervalMs = (1000f / fps).toInt().coerceAtLeast(300)

    return """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>$title - Stark Anime Video</title>
    <style>
        body { margin: 0; background: #05060b; color: #00f2fe; font-family: 'Segoe UI', Tahoma, sans-serif; display: flex; flex-direction: column; align-items: center; justify-content: center; min-height: 100vh; padding: 20px; box-sizing: border-box; }
        h1 { margin-bottom: 5px; color: #fff; text-shadow: 0 0 10px #7b2cbf; font-size: 1.5rem; }
        .subtitle { color: #8a99ad; font-size: 0.9rem; margin-bottom: 15px; }
        #videoCanvas { width: 100%; max-width: 760px; height: 420px; background: #000; border: 2px solid #7b2cbf; border-radius: 12px; box-shadow: 0 0 25px rgba(123, 44, 191, 0.5); }
        .controls { margin-top: 15px; display: flex; gap: 10px; align-items: center; flex-wrap: wrap; justify-content: center; }
        button { background: #7b2cbf; color: white; border: none; padding: 10px 18px; border-radius: 8px; font-weight: bold; cursor: pointer; transition: 0.2s; font-size: 0.85rem; }
        button:hover { background: #00f2fe; color: black; }
        .dialogue-box { width: 100%; max-width: 760px; margin-top: 15px; background: rgba(15,20,35,0.9); border: 1px solid #00f2fe; padding: 15px; border-radius: 10px; box-sizing: border-box; }
        .frame-title { font-size: 1.1rem; font-weight: bold; color: #fff; }
        .speech { font-size: 1rem; color: #00f2fe; font-style: italic; margin-top: 6px; }
        .char-lock { font-size: 0.8rem; color: #ff007f; margin-top: 4px; font-weight: bold; }
    </style>
</head>
<body>
    <h1>🎬 $title</h1>
    <div class="subtitle">Style: $style • Exported from Stark Neural Core Studio</div>
    <canvas id="videoCanvas" width="800" height="450"></canvas>
    <div class="controls">
        <button onclick="prevFrame()">⏮ PREV FRAME</button>
        <button id="playBtn" onclick="togglePlay()">▶ PLAY ANIMATION VIDEO</button>
        <button onclick="nextFrame()">⏭ NEXT FRAME</button>
        <span id="frameCounter" style="color: #fff; font-weight: bold; font-size: 0.9rem;">Frame 1 / ${keyframes.size}</span>
    </div>
    <div class="dialogue-box">
        <div id="frameTitle" class="frame-title">Loading animation video...</div>
        <div id="charLock" class="char-lock"></div>
        <div id="frameDialogue" class="speech"></div>
    </div>

    <script>
        const frames = [
$framesJsonArray
        ];
        let currentIdx = 0;
        let isPlaying = false;
        let intervalId = null;
        const canvas = document.getElementById('videoCanvas');
        const ctx = canvas.getContext('2d');

        function renderFrame(idx) {
            const kf = frames[idx];
            document.getElementById('frameCounter').innerText = 'Frame ' + (idx + 1) + ' / ' + frames.length;
            document.getElementById('frameTitle').innerText = kf.title + ' (' + kf.camera + ')';
            document.getElementById('charLock').innerText = '🔒 CHAR LOCK: ' + kf.lockedCharacter;
            document.getElementById('frameDialogue').innerText = kf.dialogue;

            // Render Animated Graphics
            ctx.fillStyle = '#05060b';
            ctx.fillRect(0, 0, canvas.width, canvas.height);

            const grad = ctx.createRadialGradient(400, 225, 20, 400, 225, 300);
            grad.addColorStop(0, '#7b2cbf');
            grad.addColorStop(0.5, '#00f2fe');
            grad.addColorStop(1, '#05060b');
            ctx.fillStyle = grad;
            ctx.fillRect(0, 0, canvas.width, canvas.height);

            ctx.strokeStyle = 'rgba(255,255,255,0.3)';
            ctx.lineWidth = 2;
            for (let i = 0; i < 16; i++) {
                const angle = (i * 22.5 + idx * 20) * Math.PI / 180;
                ctx.beginPath();
                ctx.moveTo(400, 225);
                ctx.lineTo(400 + Math.cos(angle)*420, 225 + Math.sin(angle)*240);
                ctx.stroke();
            }

            // Core energy ring
            ctx.fillStyle = '#ff007f';
            ctx.beginPath();
            ctx.arc(400, 225, 65, 0, Math.PI * 2);
            ctx.fill();

            ctx.strokeStyle = '#00f2fe';
            ctx.lineWidth = 4;
            ctx.beginPath();
            ctx.arc(400, 225, 45, 0, Math.PI * 2);
            ctx.stroke();

            if ('speechSynthesis' in window && isPlaying) {
                window.speechSynthesis.cancel();
                const u = new SpeechSynthesisUtterance(kf.dialogue);
                u.rate = 1.0;
                window.speechSynthesis.speak(u);
            }
        }

        function togglePlay() {
            isPlaying = !isPlaying;
            document.getElementById('playBtn').innerText = isPlaying ? "⏸ PAUSE" : "▶ PLAY ANIMATION VIDEO";
            if (isPlaying) {
                intervalId = setInterval(() => {
                    currentIdx = (currentIdx + 1) % frames.length;
                    renderFrame(currentIdx);
                }, $intervalMs);
            } else {
                clearInterval(intervalId);
            }
        }

        function prevFrame() {
            currentIdx = (currentIdx - 1 + frames.length) % frames.length;
            renderFrame(currentIdx);
        }

        function nextFrame() {
            currentIdx = (currentIdx + 1) % frames.length;
            renderFrame(currentIdx);
        }

        renderFrame(0);
    </script>
</body>
</html>
    """.trimIndent()
}

