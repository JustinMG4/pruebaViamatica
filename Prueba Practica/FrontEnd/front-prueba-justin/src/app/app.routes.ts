import { Routes } from '@angular/router';
import { authGuard } from './core/guard/auth.guard';
import { authenticatedGuard } from './core/guard/authenticated.guard';

export const routes: Routes = [
    {
        path: '',
        loadComponent: () => import('./shared/components/layout/layout.component'),
        children:[
            {
                path: 'dashboard',
                loadComponent: () => import('./business/dashboard/dashboard.component'),
                canActivate: [authGuard]
            },
            {
                path: 'profile',
                loadComponent: () => import('./business/profile/profile.component'),
                canActivate: [authGuard]
            },
            {
                path: '',
                redirectTo: '/dashboard',
                pathMatch: 'full'
            }
        ]
    },
    {
        path: 'login',
        loadComponent: () => import('./business/authentication/login/login.component'),
        canActivate: [authenticatedGuard]
    },
    {
        path: 'register',
        loadComponent: () => import('./business/authentication/register/register.component')
    },
    {
        path: '**',
        redirectTo: '/dashboard',
    }
];
