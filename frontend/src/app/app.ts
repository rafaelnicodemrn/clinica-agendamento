import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

// Componente raiz: so tem o menu (navbar) e o <router-outlet>,
// que é onde o Angular Router troca as telas conforme a rota
@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.html',
})
export class App {
  titulo = 'Clínica Saúde+';
}
