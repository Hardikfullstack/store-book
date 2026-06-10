package com.storebook.inventoryapp.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.storebook.inventoryapp.R
import com.storebook.inventoryapp.ui.components.CustomIconButton
import com.storebook.inventoryapp.ui.components.CustomMainButton
import com.storebook.inventoryapp.ui.theme.DMSans
import com.storebook.inventoryapp.ui.theme.SystemBarsConfig
import com.storebook.inventoryapp.ui.theme.isAppDarkMode
import com.storebook.inventoryapp.utils.LanguageItem
import com.storebook.inventoryapp.utils.getSupportedLanguages
@Composable
fun LanguageScreen(
    currentLanguageId: String = "en",
    isFirstTime: Boolean = false,
    onBack: () -> Unit = {},
    onLanguageSelected: (String) -> Unit
) {
    if (isFirstTime) {
        BackHandler {}
    }

    var selectedLanguageCode by remember(currentLanguageId) { mutableStateOf(currentLanguageId) }
    val isDarkMode = isAppDarkMode
    SystemBarsConfig(isDarkMode = isDarkMode)
    val languages = getSupportedLanguages()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 20.dp, top = 15.dp, bottom = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (!isFirstTime) {
                CustomIconButton(
                    iconRes = R.drawable.ic_back_arrow,
                    modifier = Modifier
                        .size(36.dp),
                    size = 24,
                    onClick = onBack,
                    tint = if(isDarkMode) Color.White else Color.Unspecified
                )
                Spacer(
                    modifier = Modifier
                        .width(10.dp)
                )
            }
            Text(
                text = stringResource(id = R.string.title_language),
                fontSize = 22.sp,
                fontFamily = DMSans,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .weight(1f)
            )
            CustomMainButton(
                text = stringResource(id = R.string.btn_done),
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(colorResource(R.color.red_text))
                    .clickable {
                        onLanguageSelected(selectedLanguageCode)
                    }
                    .padding(horizontal = 24.dp, vertical = 8.dp),
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            contentPadding = PaddingValues(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(languages) { language ->
                LanguageRow(
                    language = language,
                    isSelected = language.id == selectedLanguageCode,
                    onClick = { selectedLanguageCode = language.id },
                    isDarkMode = isDarkMode
                )
            }
        }
    }
}

@Composable
fun LanguageRow(
    language: LanguageItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    isDarkMode: Boolean
) {
    val borderColor = if (isSelected) colorResource(id = R.color.red_text)
                      else if(!isDarkMode) Color(0xFFF2F2F2)
                      else  Color(0xFF303030)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.background)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = language.iconResId),
            contentDescription = "${language.name} flag",
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(
            modifier = Modifier
                .width(16.dp)
        )

        Row(
            modifier = Modifier
                .weight(1f)
        ) {
            Text(
                text = language.name,
                fontSize = 16.sp,
                fontFamily = DMSans,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(
                modifier = Modifier
                    .width(4.dp)
            )
            Text(
                text = "(${language.nativeName})",
                fontSize = 16.sp,
                fontFamily = DMSans,
                fontWeight = FontWeight.Normal,
                color = colorResource(id = R.color.gray_text)
            )
        }

        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .border(
                    width = 2.dp,
                    color = if (isSelected) {
                        colorResource(id = R.color.red_text)
                    } else {
                        if(isDarkMode) Color.White else Color(0xFFB4B4B4)
                    },
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(colorResource(id = R.color.red_text))
                )
            }
        }
    }
}
