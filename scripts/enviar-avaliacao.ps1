<#
  Envia uma avaliacao de teste para a API (uso na demonstracao/video).

  Exemplos:
    .\scripts\enviar-avaliacao.ps1 -Descricao "Aula confusa" -Nota 2     # CRITICA -> chega e-mail
    .\scripts\enviar-avaliacao.ps1 -Descricao "Aula excelente" -Nota 9   # BAIXA
    .\scripts\enviar-avaliacao.ps1 -Descricao "Teste invalido" -Nota 11  # 400
#>
param(
  [Parameter(Mandatory = $true)][string]$Descricao,
  [Parameter(Mandatory = $true)][int]$Nota,
  [string]$Stack = "feedback-plataforma",
  [string]$Region = "us-east-2"
)

$ErrorActionPreference = "Stop"

$url = aws cloudformation describe-stacks --stack-name $Stack --region $Region `
  --query "Stacks[0].Outputs[?OutputKey=='ApiUrl'].OutputValue" --output text

# A chave de API e criada pelo SAM junto com o usage plan "<stack>-plano".
$planoId = aws apigateway get-usage-plans --region $Region `
  --query "items[?name=='$Stack-plano'].id | [0]" --output text
$chave = aws apigateway get-usage-plan-keys --usage-plan-id $planoId --region $Region `
  --query "items[0].value" --output text

$corpo = @{ descricao = $Descricao; nota = $Nota } | ConvertTo-Json -Compress
$bytes = [System.Text.Encoding]::UTF8.GetBytes($corpo)

try {
  $resp = Invoke-WebRequest -Uri $url -Method POST -Body $bytes -ContentType "application/json; charset=utf-8" `
    -Headers @{ "x-api-key" = $chave } -UseBasicParsing
  Write-Host "HTTP $($resp.StatusCode)"
  Write-Host $resp.Content
}
catch {
  $r = $_.Exception.Response
  if ($r) {
    Write-Host "HTTP $([int]$r.StatusCode)"
    $leitor = New-Object System.IO.StreamReader($r.GetResponseStream())
    Write-Host $leitor.ReadToEnd()
  } else { throw }
}
