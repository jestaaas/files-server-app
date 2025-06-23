document.addEventListener("DOMContentLoaded", () => {
  const uploadForm = document.getElementById("uploadForm");
  const fileInput = document.getElementById("fileInput");
  const uploadStatus = document.getElementById("uploadStatus");
  const statusText = document.getElementById("statusText");
  const progressBar = document.getElementById("progressBar");
  const resultDiv = document.getElementById("result");
  const downloadLinkInput = document.getElementById("downloadLink");
  const copyButton = document.getElementById("copyButton");

  uploadForm.addEventListener("submit", (e) => {
    e.preventDefault();

    const file = fileInput.files[0];
    if (!file) {
      return;
    }

    resultDiv.classList.add("hidden");
    uploadStatus.classList.remove("hidden");
    progressBar.style.width = "0%";
    statusText.textContent = `Загрузка файла: ${file.name}`;

    const xhr = new XMLHttpRequest();

    xhr.upload.addEventListener("progress", (event) => {
      if (event.lengthComputable) {
        const percentComplete = (event.loaded / event.total) * 100;
        progressBar.style.width = percentComplete.toFixed(2) + "%";
      }
    });

    xhr.addEventListener("load", () => {
      if (xhr.status === 200) {
        statusText.textContent = "Загрузка завершена!";
        downloadLinkInput.value = xhr.responseText;
        resultDiv.classList.remove("hidden");
      } else {
        statusText.textContent = `Ошибка загрузки: ${
          xhr.statusText || "Сервер не отвечает"
        }`;
        progressBar.style.backgroundColor = "#dc3545"; // Красный цвет для ошибки
      }
    });

    xhr.addEventListener("error", () => {
      statusText.textContent = "Ошибка сети при загрузке.";
      progressBar.style.backgroundColor = "#dc3545";
    });

    // Указываем полный URL нашего бэкенда
    xhr.open("POST", "http://localhost:8080/upload", true);

    // Устанавливаем специальный заголовок с именем файла
    xhr.setRequestHeader("X-File-Name", encodeURIComponent(file.name));

    xhr.send(file);
  });

  copyButton.addEventListener("click", () => {
    downloadLinkInput.select();
    document.execCommand("copy");
    copyButton.textContent = "Готово!";
    setTimeout(() => {
      copyButton.textContent = "Копировать";
    }, 1500);
  });
});
