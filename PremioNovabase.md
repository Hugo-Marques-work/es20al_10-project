# Continuous Delivery/Deployment

O deploy foi feito na Google Cloud Platform. Na GCP recorremos a uma VM principal, que trata de servir o frontend e o backend, é possível aceder ao website através de http://34.105.136.196/. A instância provavelmente estará desligada (para reduzir os custos), e, portanto, será preciso notificar o grupo para o site ficar novamente disponível.

Embora tivessem sido disponibilizadas Docker Images, que possivelmente facilitariam o deployment, optou-se por correr tudo nativamente nas VMs, uma vez que não havia grande dificuldade a nível de dependências e sempre se consegue obter um performance ligeiramente melhor, embora os containers sejam relativamente rápidos.

## Backend
Para o backend foi criado um serviço (`systemd`) es-backend.service que foi inicializado com `systemctl enable es-backend`. O serviço limita-se a correr o `.jar` gerado pelo comando `mvn package -Pprod`.

A escolha de utilizar um serviço traz algumas vantagens, como a facilidade de meter o servidor a correr assim que a VM liga (através do `enable` referido anteriormente), tal como os `logs` que são mantidos automaticamente através que podem ser observados com `systemctl status es-backend` ou através de `journalctl -u es-backend`.

## Frontend
O frontend é servido por um servidor `nginx` que serve a a diretoria gerada pelo comando `npm run build`. Os ficheiros de configuração do `nginx` são adaptações do dump fornecido na página da cadeira.

## Base de Dados
A Base de Dados foi realizada numa instância diferente da VM principal, recorrendo ao serviço de **Cloud SQL**, onde foi criada uma instância a correr o `PostgresSQL 12`. Optar por correr a base de dados numa instância dedicada a armazenamento traz obviamente as suas vantagens, uma vez que conseguimos ter muita mais noção dos dados que importam a uma base de dados e simultâneamente também trouxe a vantagem de não ser necessária à VM principal servir também a Base de Dados, o que se poderia tornar complicado devido aos recursos de computação limitados.

## Automação dos Deployments
A VM tem, dentro dela, um script `deploy.sh`. Este script trata de atualizar a versão do código que está nela mesma.

Resumidamente:
- Vai buscar a versão mais recente do código (`git pull`);
- Compila o backend e passa a correr o novo backend (`systemctl stop es-backend; mvn clean package -Pprod; systemctl start es-backend`);
- Prepara o novo frontend e mete na pasta servida pelo `nginx` (`npm ci; npm build; cp -r dist/ /app`).
- Se falhar em algum destes passos, o deployment falha e o administrador é notificado por email, através do comando `ssmtp`, para onde é enviado também o log do deployment, para que possa ser analizada a mensagem de erro.

![Report Example](https://web.tecnico.ulisboa.pt/ist189471/ES/failure-report.png)

## Integração com o GitHub
A maior parte das ferramentas de CI já estavam implementadas no `github`, com especial destaque para os testes automáticos. Para além dos testes automáticos, as ferramentas como os `Issues` e os `Projects` são ótimas ferramentas para manter a organização dentro da equipa, e considerámos que fossem adequadas à dimensão da equipa.

Utilizamos também as `GitHub Actions` para permitir o `Continous Deployment`, recorrendo a um agente `ssh`. A ação limita-se a fazer ssh para a VM principal e a correr o script `deploy.sh`. Para isto, foi necessário guardar como segredo (dentro do github) uma chave privada que permitisse fazer `ssh` para a máquina remota. Caso a ação não consiga fazer `ssh` então falha, no entanto, a ação não tem o poder de só por si dizer que o deploy foi bem ou mal sucedido, e para isso deve ser seguido através do email, tal como mencionado na secção anterior.

# Sobre o Quiz-Tutor
Foi atualizado para estar a par com a referência mais recente (em https://github.com/socialsoftware/quizzes-tutor). Não foi adicionada nenhuma nova *feature* uma vez que já tinham sido implementadas na última entrega. Destacam-se:

## Torneio mais interativo
Pretendemos tornar o torneio mais do que um simples Quiz, e achamos que uma boa maneira de o fazer era tornando-lo mais parecido com um "jogo" do que um momento de avaliação. Ao responder a uma pergunta do torneio, o utilizador é instantaneamente notificado se acertou ou falhou a pergunta, dando um maior sentido de recompensa ao "jogador" e tornando a experiência em algo mais interativo. O backend apenas indica se acertou a pergunta ou não, e, tendo em conta que as perguntas de um torneio só podem ser respondidas uma vez, não é possível dar a volta ao sistema (forjando pedidos `http`, por exemplo) para saber em antemão qual a resposta correta.

![Correct Response Example](https://web.tecnico.ulisboa.pt/ist189471/ES/CorrectAnswer.png)

## Leaderboard
Para complementar o sentido de "jogo" ao torneio decidiu-se implementar uma leaderboard, para que os colegas possam competir entre si e ver queem acertou mais perguntas dentro de um torneio.

![Leaderboard Example](http://web.tecnico.ulisboa.pt/~ist189460/es20/esImages/leaderboard/leaderboardPic)