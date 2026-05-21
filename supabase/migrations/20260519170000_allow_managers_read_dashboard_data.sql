-- Allow gestores to read and manage the data needed by their dashboard.

create or replace function public.current_user_public_id()
returns integer
language sql
security definer
set search_path = public
stable
as $$
  select u.id
  from public.users u
  where u.email = auth.jwt() ->> 'email'
  limit 1
$$;

create or replace function public.current_user_is_gestor()
returns boolean
language sql
security definer
set search_path = public
stable
as $$
  select exists (
    select 1
    from public.users u
    where u.email = auth.jwt() ->> 'email'
      and upper(u.role) = 'GESTOR'
      and upper(u.status) in ('ATIVO', 'ACTIVO')
  )
$$;

create or replace function public.current_user_manages_project(p_project_id integer)
returns boolean
language sql
security definer
set search_path = public
stable
as $$
  select exists (
    select 1
    from public.projetos p
    where p.id = p_project_id
      and p.gestor_id = public.current_user_public_id()
  )
$$;

create or replace function public.user_is_regular_user(p_user_id integer)
returns boolean
language sql
security definer
set search_path = public
stable
as $$
  select exists (
    select 1
    from public.users u
    where u.id = p_user_id
      and upper(u.role) = 'UTILIZADOR'
  )
$$;

create or replace function public.user_belongs_to_project(p_user_id integer, p_project_id integer)
returns boolean
language sql
security definer
set search_path = public
stable
as $$
  select exists (
    select 1
    from public.projeto_users pu
    where pu.user_id = p_user_id
      and pu.projeto_id = p_project_id
  )
$$;

create or replace function public.current_user_manages_task(p_task_id integer)
returns boolean
language sql
security definer
set search_path = public
stable
as $$
  select exists (
    select 1
    from public.tarefas t
    where t.id = p_task_id
      and public.current_user_manages_project(t.projeto_id)
  )
$$;

grant execute on function public.current_user_public_id() to authenticated;
grant execute on function public.current_user_is_gestor() to authenticated;
grant execute on function public.current_user_manages_project(integer) to authenticated;
grant execute on function public.user_is_regular_user(integer) to authenticated;
grant execute on function public.user_belongs_to_project(integer, integer) to authenticated;
grant execute on function public.current_user_manages_task(integer) to authenticated;

drop policy if exists "Managers can read assigned projects" on public.projetos;
drop policy if exists "Managers can complete assigned projects" on public.projetos;
drop policy if exists "Managers can read assigned project tasks" on public.tarefas;
drop policy if exists "Managers can create assigned project tasks" on public.tarefas;
drop policy if exists "Managers can update assigned project tasks" on public.tarefas;
drop policy if exists "Managers can delete assigned project tasks" on public.tarefas;
drop policy if exists "Managers can read project memberships" on public.projeto_users;
drop policy if exists "Managers can add regular users to projects" on public.projeto_users;
drop policy if exists "Managers can read task memberships" on public.tarefa_users;
drop policy if exists "Managers can add users to assigned tasks" on public.tarefa_users;
drop policy if exists "Managers can read regular users" on public.users;
drop policy if exists "Managers can read project evaluations" on public.avaliacoes;
drop policy if exists "Managers can create project evaluations" on public.avaliacoes;
drop policy if exists "Managers can update project evaluations" on public.avaliacoes;

create policy "Managers can read assigned projects"
on public.projetos
for select
to authenticated
using (
  public.current_user_is_gestor()
  and gestor_id = public.current_user_public_id()
);

create policy "Managers can complete assigned projects"
on public.projetos
for update
to authenticated
using (
  public.current_user_is_gestor()
  and gestor_id = public.current_user_public_id()
)
with check (
  public.current_user_is_gestor()
  and gestor_id = public.current_user_public_id()
);

create policy "Managers can read assigned project tasks"
on public.tarefas
for select
to authenticated
using (
  public.current_user_is_gestor()
  and public.current_user_manages_project(projeto_id)
);

create policy "Managers can create assigned project tasks"
on public.tarefas
for insert
to authenticated
with check (
  public.current_user_is_gestor()
  and public.current_user_manages_project(projeto_id)
);

create policy "Managers can update assigned project tasks"
on public.tarefas
for update
to authenticated
using (
  public.current_user_is_gestor()
  and public.current_user_manages_project(projeto_id)
)
with check (
  public.current_user_is_gestor()
  and public.current_user_manages_project(projeto_id)
);

create policy "Managers can delete assigned project tasks"
on public.tarefas
for delete
to authenticated
using (
  public.current_user_is_gestor()
  and public.current_user_manages_project(projeto_id)
);

create policy "Managers can read project memberships"
on public.projeto_users
for select
to authenticated
using (
  public.current_user_is_gestor()
  and public.current_user_manages_project(projeto_id)
);

create policy "Managers can add regular users to projects"
on public.projeto_users
for insert
to authenticated
with check (
  public.current_user_is_gestor()
  and public.current_user_manages_project(projeto_id)
  and public.user_is_regular_user(user_id)
);

create policy "Managers can read task memberships"
on public.tarefa_users
for select
to authenticated
using (
  public.current_user_is_gestor()
  and public.current_user_manages_task(tarefa_id)
);

create policy "Managers can add users to assigned tasks"
on public.tarefa_users
for insert
to authenticated
with check (
  public.current_user_is_gestor()
  and public.current_user_manages_task(tarefa_id)
  and exists (
    select 1
    from public.tarefas t
    where t.id = tarefa_id
      and public.user_belongs_to_project(user_id, t.projeto_id)
  )
);

create policy "Managers can read regular users"
on public.users
for select
to authenticated
using (
  public.current_user_is_gestor()
  and (
    upper(role) = 'UTILIZADOR'
    or id = public.current_user_public_id()
  )
);

create policy "Managers can read project evaluations"
on public.avaliacoes
for select
to authenticated
using (
  public.current_user_is_gestor()
  and public.current_user_manages_project(projeto_id)
);

create policy "Managers can create project evaluations"
on public.avaliacoes
for insert
to authenticated
with check (
  public.current_user_is_gestor()
  and public.current_user_manages_project(projeto_id)
  and public.user_is_regular_user(user_id)
  and classificacao between 0 and 5
);

create policy "Managers can update project evaluations"
on public.avaliacoes
for update
to authenticated
using (
  public.current_user_is_gestor()
  and public.current_user_manages_project(projeto_id)
)
with check (
  public.current_user_is_gestor()
  and public.current_user_manages_project(projeto_id)
  and public.user_is_regular_user(user_id)
  and classificacao between 0 and 5
);

create or replace function public.manager_delete_task(p_task_id integer)
returns void
language plpgsql
security definer
set search_path = public
as $$
begin
  if not public.current_user_is_gestor() then
    raise exception 'Apenas gestores podem eliminar tarefas.';
  end if;

  if not public.current_user_manages_task(p_task_id) then
    raise exception 'Nao tens permissao para eliminar esta tarefa.';
  end if;

  delete from public.tarefa_users
  where tarefa_id = p_task_id;

  delete from public.tarefas
  where id = p_task_id;
end;
$$;

grant execute on function public.manager_delete_task(integer) to authenticated;
