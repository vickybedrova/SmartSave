package com.example.smartsave

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class WelcomeCarouselSlide(
    val title: String,
    val subtitle: String,
    val description: String,
    val bulletPoints: List<String>,
    val imageResId: Int
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WelcomeScreen(onSetupClick: () -> Unit = {}) {
    val slides = listOf(
        WelcomeCarouselSlide(
            title = stringResource(R.string.welcome_carousel_first_title),
            subtitle = stringResource(R.string.welcome_carousel_first_subtitle),
            description = stringResource(R.string.welcome_carousel_first_message),
            bulletPoints = listOf(
                stringResource(R.string.welcome_carousel_first_bullet_one),
                stringResource(R.string.welcome_carousel_first_bullet_two),
                stringResource(R.string.welcome_carousel_first_bullet_three)
            ),
            imageResId = R.drawable.welcome_slide_pos_terminal
        ),
        WelcomeCarouselSlide(
            title = stringResource(R.string.welcome_carousel_second_title),
            subtitle = stringResource(R.string.welcome_carousel_second_subtitle),
            description = stringResource(R.string.welcome_carousel_second_message),
            bulletPoints = listOf(
                stringResource(R.string.welcome_carousel_second_bullet_one),
                stringResource(R.string.welcome_carousel_second_bullet_two),
                stringResource(R.string.welcome_carousel_second_bullet_three)
            ),
            imageResId = R.drawable.welcome_slide_shield
        ),
        WelcomeCarouselSlide(
            title = stringResource(R.string.welcome_carousel_third_title),
            subtitle = stringResource(R.string.welcome_carousel_third_subtitle),
            description = stringResource(R.string.welcome_carousel_third_message),
            bulletPoints = listOf(
                stringResource(R.string.welcome_carousel_third_bullet_one),
                stringResource(R.string.welcome_carousel_third_bullet_two)
            ),
            imageResId = R.drawable.welcome_slide_calculator
        )
    )

    val pagerState = rememberPagerState(pageCount = { slides.size })

    val coroutineScope = rememberCoroutineScope()
    var isUserScrolling by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(4000L)

            if (!pagerState.isScrollInProgress && !isUserScrolling) {
                val nextPage = if (pagerState.currentPage < slides.lastIndex) {
                    pagerState.currentPage + 1
                } else {
                    0
                }

                coroutineScope.launch {
                    pagerState.animateScrollToPage(nextPage)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { page ->
            WelcomeView(slide = slides[page])
        }

        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            slides.forEachIndexed { index, _ ->
                val selected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(if (selected) 10.dp else 6.dp)
                        .background(
                            color = if (selected) MaterialTheme.colorScheme.primary else Color.Gray,
                            shape = CircleShape
                        )
                )
            }
        }

        Button(
            onClick = onSetupClick,
            shape = RoundedCornerShape(32.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = stringResource(R.string.landing_button),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onPrimary
)
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun WelcomeView(slide: WelcomeCarouselSlide) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = slide.title,
            style = typography.headlineLarge.copy(lineHeight = 36.sp),
            color = colors.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = slide.subtitle + "\n\n" + slide.description,
            style = typography.bodyLarge.copy(lineHeight = 24.sp),
            color = colors.onBackground.copy(alpha = 0.95f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        Image(
            painter = painterResource(id = slide.imageResId),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .heightIn(max = 180.dp)
        )

        Spacer(modifier = Modifier.height(35.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            slide.bulletPoints.forEach {
                Text(
                    text = "✔ $it",
                    style = typography.bodyLarge,
                    color = colors.onBackground
                )
            }
        }
    }
}
