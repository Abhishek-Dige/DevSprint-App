package com.example.fight_the_randomness

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.fight_the_randomness.ui.theme.Fight_the_randomnessTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Fight_the_randomnessTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    RandomnessJourney(Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun RandomnessJourney(modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // --- APP STATE ---
    var step by remember { mutableStateOf(0) }
    var mainText by remember { mutableStateOf("Loading Chaos...") }
    var subText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // IMAGE STATE (References to your Drawable folder)
    // IMPORTANT: Change these names to match your actual file names in the drawable folder!
    var currentAvatar by remember { mutableStateOf(R.drawable.kanye) }
    var resultImageUrl by remember { mutableStateOf("") } // For dynamic API images (cats/yesno)

    // TRIVIA STATE
    var triviaAnswer by remember { mutableStateOf("") }
    var userGuess by remember { mutableStateOf("") }

    // Helper function to fetch data
    fun fetchData(url: String, key: String, onComplete: (String) -> Unit) {
        isLoading = true
        scope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    val response = URL(url).readText()
                    if (url.contains("opentdb")) {
                        val json = JSONObject(response).getJSONArray("results").getJSONObject(0)
                        triviaAnswer = json.getString("correct_answer")
                        json.getString("question").replace("&quot;", "\"").replace("&#039;", "'")
                    } else if (url.contains("thecatapi")) {
                        JSONObject(response.removePrefix("[").removeSuffix("]")).getString("url")
                    } else if (url.contains("yesno")) {
                        JSONObject(response).getString("image")
                    } else if (url.contains("geek-jokes")) {
                        JSONObject(response).getString("joke")
                    } else if (url.contains("adviceslip")) {
                        JSONObject(response).getJSONObject("slip").getString("advice")
                    } else {
                        JSONObject(response).getString(key)
                    }
                }
                onComplete(result)
            } catch (e: Exception) {
                mainText = "Error: Chaos failed. Check Internet!"
            } finally {
                isLoading = false
            }
        }
    }

    // Initial Load
    LaunchedEffect(Unit) {
        fetchData("https://api.kanye.rest", "quote") { mainText = "Kanye: \"$it\"" }
    }

    Column(
        modifier = modifier.fillMaxSize().padding(20.dp).verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("FIGHT THE RANDOMNESS", fontSize = 24.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)

        Spacer(Modifier.height(20.dp))

        // --- OFFLINE CHARACTER IMAGE (From Drawables) ---
        Box(contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(id = currentAvatar),
                contentDescription = "Character",
                modifier = Modifier.size(150.dp).clip(CircleShape).background(Color.Gray),
                contentScale = ContentScale.Crop
            )
            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(160.dp))
        }

        Spacer(Modifier.height(20.dp))

        // --- MAIN DIALOGUE ---
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(mainText, fontSize = 18.sp, textAlign = TextAlign.Center)
                if (subText.isNotEmpty()) {
                    Text("\n$subText", fontSize = 14.sp, color = Color.Gray, textAlign = TextAlign.Center)
                }
            }
        }

        // --- TRIVIA INPUT SECTION ---
        if (step == 4) {
            Spacer(Modifier.height(20.dp))
            OutlinedTextField(
                value = userGuess,
                onValueChange = { userGuess = it },
                label = { Text("Answer the Trivia...") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // --- DYNAMIC API IMAGES (Cats/YesNo) ---
        if (resultImageUrl.isNotEmpty()) {
            Spacer(Modifier.height(20.dp))
            AsyncImage(
                model = resultImageUrl,
                contentDescription = "Result Image",
                modifier = Modifier.fillMaxWidth().height(200.dp).clip(CardDefaults.shape),
                contentScale = ContentScale.Fit
            )
        }

        Spacer(Modifier.height(30.dp))

        // --- BUTTONS ---
        if (!isLoading) {
            when (step) {
                0 -> Button(onClick = {
                    fetchData("https://evilinsult.com/generate_insult.php?lang=en&type=json", "insult") {
                        mainText = "Kanye Insults You: $it"
                        step = 1
                    }
                }) { Text("Greet Kanye") }

                1 -> Button(onClick = {
                    fetchData("https://api.chucknorris.io/jokes/random", "value") {
                        mainText = "Chuck Norris: \"Stop it! $it\""
                        currentAvatar = R.drawable.chuck_norris // Change to your drawable name
                        step = 2
                    }
                }) { Text("Insult Him Back") }

                2 -> Button(onClick = {
                    fetchData("https://geek-jokes.sameerkumar.website/api?format=json", "joke") {
                        mainText = "Rick enters the scene..: $it"
                        currentAvatar = R.drawable.rick // Change to your drawable name
                        step = 3
                    }
                }) { Text("Whatever...gimme something interesting") }

                3 -> Button(onClick = {
                    fetchData("https://opentdb.com/api.php?amount=1", "") {
                        mainText = "TRIVIA: $it"
                        step = 4
                    }
                }) { Text("Get Nerdy") }

                4 -> Button(onClick = {
                    val isCorrect = userGuess.trim().equals(triviaAnswer, ignoreCase = true)
                    if (isCorrect) {
                        fetchData("https://api.thecatapi.com/v1/images/search", "") {
                            resultImageUrl = it
                            mainText = "CORRECT! Look at this cat."
                            step = 5
                        }
                    } else {
                        mainText = "WRONG! The answer was $triviaAnswer. Kanye is laughing at you."
                        currentAvatar = R.drawable.kanye
                        step = 5
                    }
                }) { Text("Submit Answer") }

                5 -> Button(onClick = {
                    fetchData("https://api.adviceslip.com/advice", "") {
                        mainText = "Drunk Man's life changing advice...: \"$it\""
                        resultImageUrl = ""
                        currentAvatar = R.drawable.drunk_guy // Change to your drawable name
                        step = 6
                    }
                }) { Text("okay..?") }

                6 -> Button(onClick = {
                    fetchData("https://yesno.wtf/api", "") {
                        resultImageUrl = it
                        mainText = "SEE THE PRIZEEE..."
                        step = 7
                    }
                }) { Text("THE FINAL SURPRISE(yeeeeeeee)") }

                7 -> Button(onClick = {
                    step = 0; resultImageUrl = ""; userGuess = ""
                    currentAvatar = R.drawable.kanye
                    fetchData("https://api.kanye.rest", "quote") { mainText = "Kanye: $it" }
                }) { Text("RESTART CHAOS") }
            }
        }
    }
}