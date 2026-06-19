package anyang.mypackages.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import anyang.mypackages.ui.theme.*

@Composable
fun SearchBar(
    searchText: String,
    onSearchChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "搜索柜号、位置、取件码..."
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(FillTertiary)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "搜索",
                tint = TertiaryLabel,
                modifier = Modifier.size(16.dp)
            )

            Spacer(Modifier.width(6.dp))

            Box(modifier = Modifier.weight(1f)) {
                if (searchText.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TertiaryLabel,
                        fontSize = MaterialTheme.typography.bodyMedium.fontSize
                    )
                }
                BasicTextField(
                    value = searchText,
                    onValueChange = onSearchChange,
                    textStyle = TextStyle(
                        color = Label,
                        fontSize = MaterialTheme.typography.bodyMedium.fontSize
                    ),
                    cursorBrush = SolidColor(SystemBlue),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (searchText.isNotEmpty()) {
                IconButton(
                    onClick = { onSearchChange("") },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "清除",
                        tint = TertiaryLabel,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
