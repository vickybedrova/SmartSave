package com.example.smartsave.ui.activity

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.smartsave.R
import com.example.smartsave.ui.navigation.Screen
import com.example.smartsave.ui.theme.blue

@Composable
fun LandingScreen(navController: NavController) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.landing_page_title),
                    style = typography.headlineLarge,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = stringResource(R.string.smartsave),
                    style = typography.headlineLarge,
                    textAlign = TextAlign.Center,
                    color = blue
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(top = 6.dp)
                ) {
                    Text(
                        text = "by",
                        style = typography.bodyLarge.copy(
                            fontSize = 16.sp,
                            color = colors.onBackground
                        )
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "myPOS logo",
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Text(
                text = stringResource(R.string.landing_page_message),
                style = typography.bodyLarge.copy(fontSize = 18.sp),
                color = colors.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Button(
                onClick = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                shape = RoundedCornerShape(32.dp),
                colors = ButtonDefaults.buttonColors(containerColor = blue),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = stringResource(R.string.landing_button),
                    style = typography.labelLarge.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = colors.onPrimary
                )
            }

            Image(
                painter = painterResource(id = R.drawable.landing_piggy_bank),
                contentDescription = "Piggy Bank",
                modifier = Modifier
                    .fillMaxWidth(1f)
                    .padding(top = 14.dp)
            )
        }
    }
}