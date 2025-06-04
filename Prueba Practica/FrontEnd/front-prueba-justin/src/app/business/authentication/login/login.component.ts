import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../../core/services/auth.service';
import { Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export default class LoginComponent implements OnInit {
  loginForm!: FormGroup;

  constructor(private fb: FormBuilder, private authService: AuthService, private router: Router) { }

  ngOnInit(): void {
    this.loginForm = this.fb.group({
      username: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required]
    });
  }

  login(): void {
    if (this.loginForm.invalid) {
      Swal.fire({
        icon: 'warning',
        title: 'Error',
        text: 'Username y password no pueden estar vacios.',
      });
      return;
    }

    const { username, password } = this.loginForm.value;
    this.authService.login(username, password).subscribe({
      next: (response) => {
        if (response && response.jwt) {
          Swal.fire({
            icon: 'success',
            title: 'Login exitoso',
            text: 'Bienvenido al sistema.',
            timer: 1500,
            showConfirmButton: false,
          });
          this.router.navigate(['/dashboard']);

        } else {
          Swal.fire({
            icon: 'error',
            title: 'Credenciales incorrectas',
            text: 'Usuario o contraseña inválidos.'
          });
        }
      },
      error: () => {
        Swal.fire({
          icon: 'error',
          title: 'Credenciales incorrectas',
          text: 'Usuario o contraseña inválidos.'
        });
      }
    });
  }
}
