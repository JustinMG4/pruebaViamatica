import { Component } from '@angular/core';
import { AuthService } from '../../../core/services/auth.service';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-header',
  imports: [],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent {

  constructor(private authService: AuthService) {}

  logout(): void {
    this.authService.logout();
    Swal.fire({
      icon: 'success',
      title: 'Cierre de sesión exitoso',
      text: 'Hasta luego, vuelve pronto!'
    });
  }

}
