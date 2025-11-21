## set up for windows
$env:KUBECONFIG="hihi.yaml"
Set-Alias -Name k -Value kubectl
k get nodes


## set up for linux
export KUBECONFIG="dsm.yaml"
alias k="kubectl"
k get nodes
