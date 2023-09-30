@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.androidappweek1

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.Dimension
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navArgument
import androidx.navigation.compose.rememberNavController
import com.example.androidappweek1.ui.theme.AndroidAppWeek1Theme
import kotlinx.coroutines.NonDisposableHandle.parent
data class Post(
    val userId: Int,
    val id: Int,
    val title: String,
    val body: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "login"
                ) {
                    composable("login") { LoginScreen(navController) }
                    composable("details/{username}/{password}",
                        arguments = listOf(
                            navArgument("username") { type = NavType.StringType },
                            navArgument("password") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val username =
                            backStackEntry.arguments?.getString("username") ?: "No Username"
                        val password =
                            backStackEntry.arguments?.getString("password") ?: "No Password"
                        DetailsScreen(username, password)
                    }
                }
            }
        }
    }
}

@Composable
fun MyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavHostController) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        val (logo, usernameTextField, passwordTextField, loginButton) = createRefs()

        // Logo
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .constrainAs(logo) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(usernameTextField.top, margin = 16.dp)
                }
                .fillMaxWidth()
                .height(100.dp)
        )

        // Username Text Field
        TextField(
            value = username,
            onValueChange = { username = it },
            singleLine = true,
            modifier = Modifier
                .constrainAs(usernameTextField) {
                    top.linkTo(logo.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(passwordTextField.top, margin = 8.dp)
                }
                .fillMaxWidth()
        )

        // Password Text Field
        TextField(
            value = password,
            onValueChange = { password = it },
            singleLine = true,
            modifier = Modifier
                .constrainAs(passwordTextField) {
                    top.linkTo(usernameTextField.bottom, margin = 8.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(loginButton.top, margin = 16.dp)
                }
                .fillMaxWidth()
        )

        // Login Button
        Button(
            onClick = {
                // Pass the username and password to the next screen
                navController.navigate("details/$username/$password")
            },
            modifier = Modifier
                .constrainAs(loginButton) {
                    top.linkTo(passwordTextField.bottom, margin = 16.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                }
                .fillMaxWidth()
        ) {
            Text(text = "Login")
        }
    }
}

interface JsonPlaceholderApi {
    @GET("posts")
    suspend fun getPosts(): List<Post>
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun DetailsScreen(username: String, password: String) {
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Create a Retrofit instance
    val retrofit = Retrofit.Builder()
        .baseUrl("https://jsonplaceholder.typicode.com/")
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    // Create an instance of the JsonPlaceholderApi
    val api = retrofit.create(JsonPlaceholderApi::class.java)

    // Make the API call when the composable is first displayed
    LaunchedEffect(username) {
        try {
            val response = api.getPosts()
            posts = response
            isLoading = false
        } catch (e: Exception) {
            // Handle the error
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Posts") }
            )
        },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (isLoading) {
                    // Display a loading indicator while making the API call
                    CircularProgressIndicator()
                } else if (posts.isNotEmpty()) {
                    // Display the list of posts once the API call is complete
                    Text(
                        text = "Welcome, $username!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Text(
                        text = "Your password: $password",
                        fontSize = 18.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Display the list of posts in a scrollable LazyColumn
                    LazyColumn {
                        items(posts) { post ->
                            Column(
                                modifier = Modifier.padding(bottom = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Title: ${post.title}",
                                    fontSize = 18.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Body: ${post.body}",
                                    fontSize = 18.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            // Implement any action you want here when the button is clicked.
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                    ) {
                        Text(text = "Logout")
                    }
                } else {
                    // Handle the case where the API call fails or there are no posts
                    Text(
                        text = "Failed to fetch data or no posts available",
                        fontSize = 18.sp
                    )
                }
            }
        }
    )
}


@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    MyTheme {
        LoginScreen(rememberNavController())
    }
}