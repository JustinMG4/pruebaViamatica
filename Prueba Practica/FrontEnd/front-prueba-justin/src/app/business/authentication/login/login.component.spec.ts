import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { RouterTestingModule } from '@angular/router/testing';

import LoginComponent from './login.component'; // Importa el componente como standalone
import { AuthService } from '../../../core/services/auth.service';
import { of } from 'rxjs';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let authServiceStub: Partial<AuthService>;

  beforeEach(async () => {
    authServiceStub = {
      login: (username: string, password: string) => of(true)
    };

    await TestBed.configureTestingModule({
      imports: [ReactiveFormsModule, CommonModule, RouterTestingModule, LoginComponent], // Importa el componente aquí
      providers: [{ provide: AuthService, useValue: authServiceStub }]
    })
    .compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
