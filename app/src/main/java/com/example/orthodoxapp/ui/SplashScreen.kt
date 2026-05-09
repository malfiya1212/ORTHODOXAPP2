package com.example.orthodoxapp.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(onGetStarted: () -> Unit) {
    val goldColor = Color(0xFFD4AF37)
    val deeperBlue = Color(0xFF0F172A)
    val onboardingPages = listOf(
        OnboardingData(
            "Tewahedo Church", 
            "Finance & Management", 
            "Manage church finances, church groups\nand members with transparency",
            "https://images.unsplash.com/photo-1548013146-72479768bbaa?auto=format&fit=crop&q=80&w=1000"
        ),
        OnboardingData(
            "Parish Treasury", 
            "Strategic Oversight", 
            "Monitor regional performance and diocesan\ngrowth in real-time.",
            "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?auto=format&fit=crop&q=80&w=1000"
        ),
        OnboardingData(
            "Community Unity", 
            "Mutual Aid Systems", 
            "Digitize traditional Chure groups and\nempower parish members.",
            "https://images.unsplash.com/photo-1511632765486-a01980e01a18?auto=format&fit=crop&q=80&w=1000"
        ),
        OnboardingData(
            "Audit-Ready", 
            "National Reporting", 
            "Export professional financial reports to\nPDF and Excel instantly.",
            "https://images.unsplash.com/photo-1450101499163-c8848c66ca85?auto=format&fit=crop&q=80&w=1000"
        )
    )

    val pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { onboardingPages.size })
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == onboardingPages.size - 1

    Box(modifier = Modifier.fillMaxSize()) {
        androidx.compose.foundation.pager.HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { pageIndex ->
            val page = onboardingPages[pageIndex]
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = page.image,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    deeperBlue.copy(alpha = 0.9f),
                                    deeperBlue.copy(alpha = 0.4f),
                                    Color.Transparent,
                                    deeperBlue.copy(alpha = 0.2f),
                                    deeperBlue.copy(alpha = 0.95f)
                                )
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(80.dp))
                    Surface(
                        shape = CircleShape,
                        modifier = Modifier.size(100.dp),
                        color = deeperBlue.copy(alpha = 0.6f),
                        border = BorderStroke(2.dp, goldColor)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("☦", fontSize = 56.sp, color = goldColor)
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(page.title, color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
                    Text(page.subtitle, color = goldColor, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("⊹", color = goldColor.copy(alpha = 0.8f), fontSize = 24.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(page.description, color = Color.White.copy(alpha = 0.85f), fontSize = 16.sp, textAlign = TextAlign.Center, lineHeight = 24.sp)
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp, vertical = 40.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    if (isLastPage) {
                        onGetStarted()
                    } else {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(30.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 12.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        if (isLastPage) "Get Started" else "Next", 
                        color = deeperBlue, 
                        fontWeight = FontWeight.ExtraBold, 
                        fontSize = 18.sp, 
                        modifier = Modifier.align(Alignment.Center)
                    )
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward, 
                        contentDescription = null, 
                        tint = deeperBlue, 
                        modifier = Modifier.align(Alignment.CenterEnd).size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                repeat(4) { index ->
                    Box(
                        modifier = Modifier
                            .size(if (pagerState.currentPage == index) 12.dp else 8.dp)
                            .background(
                                color = if (pagerState.currentPage == index) Color.White else Color.White.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                    )
                }
            }
        }
    }
}

data class OnboardingData(val title: String, val subtitle: String, val description: String, val image: String)
